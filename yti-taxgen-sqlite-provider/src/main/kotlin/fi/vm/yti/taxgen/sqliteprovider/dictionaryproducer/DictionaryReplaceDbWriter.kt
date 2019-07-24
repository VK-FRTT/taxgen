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
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DimensionLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DomainLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
import java.nio.file.Path
import java.sql.DriverManager
import java.sql.SQLException

class DictionaryReplaceDbWriter(
    targetDbPath: Path,
    private val diagnosticContext: DiagnosticContext
) : DpmDbWriter {
    private val targetDbPath: Path = targetDbPath.toAbsolutePath().normalize()

    override fun writeModel(dpmModel: DpmModel) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.SQLiteDbWriter,
            contextIdentifier = targetDbPath.toString()
        ) {
            FileOps.failIfTargetFileMissing(targetDbPath, diagnosticContext)
            failIfDbConnectionFails()

            SqliteOps.connectDatabase(targetDbPath)

            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            val ordinateCategorisationBinder =
                OrdinateCategorisationIntegrator.loadInitialState(diagnosticContext)

            DbDictionaries.purgeDictionaryContent()

            val dictionaryLookupItems = dpmModel.dictionaries.map {
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

            val (memberLookupItems, hierarchyLookupItems) =
                dpmModel.dictionaries
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
                fixedEntitiesLookupItem
            )
        }
    }

    private fun failIfDbConnectionFails() {
        try {
            val dbConnection = DriverManager.getConnection("jdbc:sqlite:$targetDbPath")
            dbConnection.close()
        } catch (sqlEx: SQLException) {
            diagnosticContext.fatal("Target database file open failed: ${sqlEx.message}")
        }
    }
}
