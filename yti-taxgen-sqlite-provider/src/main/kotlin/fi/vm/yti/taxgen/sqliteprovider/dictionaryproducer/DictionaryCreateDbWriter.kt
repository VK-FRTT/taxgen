package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteprovider.helpers.SqliteOps
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DictionaryCreateDbWriter(
    targetDbPath: Path,
    private val forceOverwrite: Boolean,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
    private val targetDbPath: Path = targetDbPath.toAbsolutePath().normalize()

    override fun writeModel(dpmModel: DpmModel) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.SQLiteDbWriter,
            contextIdentifier = targetDbPath.toString()
        ) {
            FileOps.deleteConflictingTargetFileIfAllowed(targetDbPath, forceOverwrite)
            FileOps.failIfTargetFileExists(targetDbPath, diagnosticContext)
            FileOps.createIntermediateFolders(targetDbPath)

            initDbFileFromSeed(targetDbPath)

            SqliteOps.connectDatabase(targetDbPath)

            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            DbDictionaries.purgeDictionaryContent()

            val dictionaryLookupItems = dpmModel.dictionaries.map {
                val ownerId = DbOwners.writeOwner(it.owner)

                DbDictionaries.writeDictionaryBaseParts(
                    it,
                    ownerId,
                    languageIds,
                    diagnosticContext
                )
            }

            val fixedEntitiesLookupItem = DbFixedEntities.writeFixedEntities(
                languageIds,
                diagnosticContext
            )

            dpmModel.dictionaries
                .zip(dictionaryLookupItems)
                .forEach { (dictionary, dictionaryLookupItem) ->
                    DbDictionaries.writeDictionaryMetricsToFixedDomain(
                        dictionary,
                        languageIds,
                        dictionaryLookupItem,
                        fixedEntitiesLookupItem.metricDomainId,
                        diagnosticContext
                    )
                }
        }
    }

    private fun initDbFileFromSeed(targetDbPath: Path) {
        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)
    }
}
