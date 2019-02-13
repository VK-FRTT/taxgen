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
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DimensionLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DomainLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
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

            val (memberLookupItems, hierarchyLookupItems) = dpmDictionaries
                .zip(dictionaryLookupItems)
                .map { (dictionary, dictionaryLookupItem) ->
                    DbDictionaries.writeDictionaryMetricsToFixedDomain(
                        dictionary,
                        languageIds,
                        dictionaryLookupItem,
                        fixedEntitiesLookupItem.metricDomainId
                    )
                }.reduce { accumulator, element ->
                    Pair(
                        accumulator.first + element.first,
                        accumulator.second + element.second
                    )
                }

            val metricDictionaryLookupItem = DpmDictionaryLookupItem(
                domainLookupItems = listOf(
                    DomainLookupItem(
                        domainCode = fixedEntitiesLookupItem.metricDomainCode,
                        memberLookupItems = memberLookupItems,
                        hierarchyLookupItems = hierarchyLookupItems,
                        domainId = fixedEntitiesLookupItem.metricDomainId
                    )
                ),
                dimensionLookupItems = listOf(
                    DimensionLookupItem(
                        dimensionXbrlCode = fixedEntitiesLookupItem.metricDimensionXbrlCode,
                        dimensionId = fixedEntitiesLookupItem.metricDimensionId
                    )
                ),
                ownerId = fixedEntitiesLookupItem.metricDomainOwnerId
            )

            ordinateCategorisationBinder.rebindAndWriteCategorisations(
                dictionaryLookupItems + metricDictionaryLookupItem,
                fixedEntitiesLookupItem,
                diagnosticContext
            )
        }
    }
}
