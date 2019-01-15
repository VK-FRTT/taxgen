package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DomainItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.HierarchyItem
import fi.vm.yti.taxgen.sqliteprovider.tables.Tables
import fi.vm.yti.taxgen.sqliteprovider.writers.DbDimensions
import fi.vm.yti.taxgen.sqliteprovider.writers.DbDomains
import fi.vm.yti.taxgen.sqliteprovider.writers.DbHierarchies
import fi.vm.yti.taxgen.sqliteprovider.writers.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.writers.DbMetric
import fi.vm.yti.taxgen.sqliteprovider.writers.DbOwners
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Path
import java.sql.Connection

class DpmDbWriter(
    rawTargetDbPath: Path,
    forceOverwrite: Boolean,
    private val diagnosticContext: DiagnosticContext
) {
    private val targetDbPath: Path = rawTargetDbPath.toAbsolutePath().normalize()

    init {
        FileOps.deleteConflictingTargetFileIfAllowed(targetDbPath, forceOverwrite)
        FileOps.failIfTargetFileExists(targetDbPath, diagnosticContext)
        FileOps.createIntermediateFolders(targetDbPath)
    }

    fun writeDpmDb(dpmDictionaries: List<DpmDictionary>) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.WriteSQLiteDb,
            contextIdentifier = targetDbPath.toString()
        ) {
            connectDatabase()
            Tables.create()
            val languageIds = DbLanguages.writeLanguages()

            dpmDictionaries.forEach {
                writeDpmDictionary(it, languageIds)
            }
        }
    }

    private fun targetSqliteDbUrl() = "jdbc:sqlite:$targetDbPath"

    private fun connectDatabase() {
        Database.connect(targetSqliteDbUrl(), "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }

    private fun writeDpmDictionary(
        dpmDictionary: DpmDictionary,
        languageIds: Map<Language, EntityID<Int>>
    ) {
        val ownerId = DbOwners.writeOwner(dpmDictionary.owner)

        val dictionaryContext = DpmDictionaryItem(
            dpmDictionary.owner,
            ownerId,
            languageIds
        )

        dpmDictionary.explicitDomains.forEach { explicitDomain ->

            val (domainId, memberIds) = DbDomains.writeExplicitDomainAndMembers(
                dictionaryContext,
                explicitDomain
            )

            val hierarchyItems = mutableListOf<HierarchyItem>()

            explicitDomain.hierarchies.forEach { hierarchy ->
                val hierarchyId = DbHierarchies.writeHierarchyAndAndNodes(
                    dictionaryContext,
                    hierarchy,
                    domainId,
                    memberIds
                )

                hierarchyItems.add(
                    HierarchyItem(
                        hierarchyCode = hierarchy.hierarchyCode,
                        hierarchyId = hierarchyId
                    )
                )
            }

            dictionaryContext.addDomainItem(
                DomainItem(
                    domainCode = explicitDomain.domainCode,
                    domainId = domainId,
                    hierarchyItems = hierarchyItems
                )
            )
        }

        dpmDictionary.typedDomains.forEach { typedDomain ->
            DbDomains.writeTypedDomain(
                dictionaryContext,
                typedDomain
            )
        }

        dpmDictionary.explicitDimensions.forEach { explicitDimension ->
            DbDimensions.writeExplicitDimension(
                dictionaryContext,
                explicitDimension
            )
        }

        dpmDictionary.typedDimensions.forEach { typedDimension ->
            DbDimensions.writeTypedDimension(
                dictionaryContext,
                typedDimension
            )
        }

        val (metricDomainId, metricHierarchyId) = DbMetric.writeMetricDomainAndHierarchy(dictionaryContext)

        dpmDictionary.metrics.forEach { metric ->
            DbMetric.writeMetric(
                dictionaryContext,
                metric,
                metricDomainId,
                metricHierarchyId
            )
        }
    }
}
