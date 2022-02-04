package fi.vm.yti.taxgen.sqliteoutput.dictionarycreate

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteoutput.DpmDbWriter
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteoutput.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteoutput.helpers.ModelTransformer
import fi.vm.yti.taxgen.sqliteoutput.helpers.SqliteOps
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import org.jetbrains.exposed.dao.id.EntityID

class DictionaryCreateDbWriter(
    outputDbPath: Path,
    private val forceOverwrite: Boolean,
    private val keepPartialOutput: Boolean,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
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

        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, outputDbPath, StandardCopyOption.REPLACE_EXISTING)
    }

    private fun doWriteModel(
        dpmModel: DpmModel,
        processingOptions: ProcessingOptions
    ) {
        DbLanguages.configureLanguages()
        val languageIds = DbLanguages.resolveLanguageIds()

        DbDictionaries.purgeDictionaryContent()

        writeDictionaryContents(
            dpmModel,
            processingOptions,
            languageIds,
            diagnosticContext
        )
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
            val ownerId = DbOwners.writeOwner(it.owner)

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
}
