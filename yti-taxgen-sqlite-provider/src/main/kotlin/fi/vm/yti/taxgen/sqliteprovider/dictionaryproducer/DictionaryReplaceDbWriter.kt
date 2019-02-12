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

class DictionaryReplaceDbWriter(
    targetDbPath: Path,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
    private val targetDbPath: Path = targetDbPath.toAbsolutePath().normalize()

    //TODO - tests
    override fun writeWithDictionaries(dpmDictionaries: List<DpmDictionary>) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.WriteSQLiteDb,
            contextIdentifier = targetDbPath.toString()
        ) {
            val stream = this::class.java.getResourceAsStream("/SBR_2019-01-22_exportable_original_from_DM.db")
            Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)

            FileOps.failIfTargetFileMissing(targetDbPath, diagnosticContext)

            SqliteOps.connectDatabase(targetDbPath)

            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            val ordinateCategorisationBinder = OrdinateCategorisationBinder.rememberInitialCategorizations()

            DbDictionaries.purgeDictionaryContent()

            val dictionaryLookupItems = dpmDictionaries.map {
                val ownerId = DbOwners.lookupOwnerIdByPrefix(it.owner, diagnosticContext)

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
                .map { (dictionary, dictionaryLookupItem) ->
                    DbDictionaries.writeDictionaryMetrics(
                        dictionary,
                        languageIds,
                        dictionaryLookupItem,
                        fixedEntitiesLookupItem
                    )
                }

            //TODO - binding doesn't locate MET dimension & members
            //Consider creating artificial DPM dictionary (with Eurofiling stub owner)
            //and place fixed MET dimension & domain & domain members under that dictionary
            ordinateCategorisationBinder.rebindAndWriteCategorisations(
                dictionaryLookupItems,
                fixedEntitiesLookupItem,
                diagnosticContext
            )
        }
    }
}
