package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteoutput.DpmDbWriter
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform.FrameworksTransform
import fi.vm.yti.taxgen.sqliteoutput.helpers.ModelTransformer
import fi.vm.yti.taxgen.sqliteoutput.helpers.SqliteOps
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.SQLException
import org.jetbrains.exposed.dao.id.EntityID

class DictionaryReplaceDbWriter(
    baselineDbPath: Path,
    outputDbPath: Path,
    private val forceOverwrite: Boolean,
    private val keepPartialOutput: Boolean,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
    private val baselineDbPath: Path = baselineDbPath.toAbsolutePath().normalize()
    private val outputDbPath: Path = outputDbPath.toAbsolutePath().normalize()

    override fun writeModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        val updatedDpmModel = ModelTransformer.transformDpmModelByProcessingOptions(
            dpmModel,
            processingOptions,
            diagnosticContext
        )

        initOutputDbFile()

        SqliteOps.connectDatabase(outputDbPath)

        doWriteModel(updatedDpmModel, processingOptions)

        if (diagnosticContext.criticalErrorsReceived()) {
            if (keepPartialOutput) {
                diagnosticContext.info("Output file is partial and has content errors")
            } else {
                Files.delete(outputDbPath)
            }
        }
    }

    override fun outputPath(): Path = outputDbPath

    private fun initOutputDbFile() {
        FileOps.deleteConflictingOutputFileIfAllowed(outputDbPath, forceOverwrite)
        FileOps.failIfOutputFileExists(outputDbPath, diagnosticContext)
        FileOps.createIntermediateFolders(outputDbPath)

        ensureBaselineDbUsable(baselineDbPath, diagnosticContext)

        Files.copy(baselineDbPath, outputDbPath)
    }

    private fun doWriteModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        DbLanguages.configureLanguages()
        val languageIds = DbLanguages.resolveLanguageIds()

        val frameworksTransform = FrameworksTransform.captureInitialState(
            diagnosticContext
        )

        DbDictionaries.purgeDictionaryContent()

        writeDictionaryContents(
            dpmModel,
            processingOptions,
            languageIds,
            diagnosticContext
        )

        frameworksTransform.updateFrameworkEntities()
    }

    private fun writeDictionaryContents(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions,
        languageIds: Map<Language, EntityID<Int>>,
        diagnosticContext: DiagnosticContext
    ) {
        diagnosticContext.withContext(
            contextType = DiagnosticContexts.DpmDictionaryWrite.toType(),
            contextDetails = null
        ) {
            doWriteDictionaryContents(
                dpmModel,
                processingOptions,
                languageIds
            )
        }
    }

    private fun doWriteDictionaryContents(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions,
        languageIds: Map<Language, EntityID<Int>>
    ) {
        val dictionaryAndOwnerIds = dpmModel.dictionaries.map {
            val ownerId = DbOwners.lookupOwnerIdByPrefix(it.owner, diagnosticContext)

            DbDictionaries.writeDictionaryBaseParts(
                it,
                ownerId,
                languageIds,
                processingOptions
            )

            Pair(it, ownerId)
        }

        val fixedEntitiesLookupItem = DbFixedEntities.writeFixedEntities(
            languageIds,
            diagnosticContext
        )

        dictionaryAndOwnerIds.forEach { (dictionary, ownerId) ->
            DbDictionaries.writeDictionaryMetricsToFixedDomain(
                dictionary,
                ownerId,
                fixedEntitiesLookupItem.metricDomainId,
                languageIds,
                processingOptions
            )
        }
    }

    companion object {
        private fun ensureBaselineDbUsable(
            baselineDpmDbPath: Path,
            diagnostic: Diagnostic
        ) {
            try {
                val dbConnection = DriverManager.getConnection("jdbc:sqlite:$baselineDpmDbPath")
                dbConnection.createStatement().executeQuery("SELECT * FROM mOwner")
                dbConnection.close()
            } catch (sqlEx: SQLException) {
                diagnostic.fatal("Baseline-db file open failed: ${sqlEx.message}")
            }
        }
    }
}
