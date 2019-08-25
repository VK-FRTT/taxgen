package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbDictionaries
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbFixedEntities
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbOwners
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform.OpenAxisValueRestrictionTransform
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform.OrdinateCategorisationTransform
import fi.vm.yti.taxgen.sqliteprovider.helpers.SqliteOps
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

            val ordinateCategorisationTransform =
                OrdinateCategorisationTransform.loadInitialState(
                    diagnosticContext
                )

            val openAxisValueRestrictionTransform =
                OpenAxisValueRestrictionTransform.loadInitialState(
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

            ordinateCategorisationTransform.transformAndWriteCategorisations()
            openAxisValueRestrictionTransform.transformAndWriteRestrictions()
        }
    }

    private fun failIfDbConnectionFails() {
        try {
            val dbConnection = DriverManager.getConnection("jdbc:sqlite:$targetDbPath")
            dbConnection.createStatement().executeQuery("SELECT * FROM mOwner")
            dbConnection.close()
        } catch (sqlEx: SQLException) {
            diagnosticContext.fatal("Target database file open failed: ${sqlEx.message}")
        }
    }
}
