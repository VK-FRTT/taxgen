package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform.FrameworksTransform
import fi.vm.yti.taxgen.sqliteprovider.helpers.SqliteOps
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.SQLException

class DictionaryReplaceDbWriter(
    baselineDbPath: Path,
    outputDbPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
    private val baselineDbPath: Path = baselineDbPath.toAbsolutePath().normalize()
    private val outputDbPath: Path = outputDbPath.toAbsolutePath().normalize()

    override fun writeModel(dpmModel: DpmModel) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.SQLiteDbWriter,
            contextIdentifier = baselineDbPath.toString()
        ) {
            FileOps.deleteConflictingOutputFileIfAllowed(outputDbPath, forceOverwrite)
            FileOps.failIfOutputFileExists(outputDbPath, diagnosticContext)
            FileOps.createIntermediateFolders(outputDbPath)

            ensureBaselineDbUsable(baselineDbPath, diagnosticContext)
            Files.copy(baselineDbPath, outputDbPath)

            SqliteOps.connectDatabase(outputDbPath)

            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            val frameworksTransform = FrameworksTransform.loadInitialState(
                diagnosticContext
            )

            DbDictionaries.purgeDictionaryContent()

            val dictionaryAndOwnerIds = dpmModel.dictionaries.map {
                val ownerId = DbOwners.lookupOwnerIdByPrefix(it.owner, diagnosticContext)

                DbDictionaries.writeDictionaryBaseParts(
                    it,
                    ownerId,
                    languageIds
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
                    languageIds
                )
            }

            frameworksTransform.transformFrameworkEntities()
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
