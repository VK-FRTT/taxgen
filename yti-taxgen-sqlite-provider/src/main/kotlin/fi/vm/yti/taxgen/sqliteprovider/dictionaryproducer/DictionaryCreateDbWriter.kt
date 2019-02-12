package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
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

    override fun writeWithDictionaries(dpmDictionaries: List<DpmDictionary>) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.WriteSQLiteDb,
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

            val dictionaryLookupItems = dpmDictionaries.map {
                val ownerId = DbOwners.writeOwner(it.owner)

                DbDictionaries.writeDictionaryBaseParts(
                    it,
                    ownerId,
                    languageIds
                )
            }

            val fixedEntitiesLookupItem = DbFixedEntities.writeFixedEntities(
                languageIds,
                diagnosticContext
            )

            dpmDictionaries
                .zip(dictionaryLookupItems)
                .forEach { (dictionary, dictionaryLookupItem) ->
                    DbDictionaries.writeDictionaryMetrics(
                        dictionary,
                        languageIds,
                        dictionaryLookupItem,
                        fixedEntitiesLookupItem
                    )
                }
        }
    }

    private fun initDbFileFromSeed(targetDbPath: Path) {
        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)
    }
}
