package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DomainItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.HierarchyItem
import fi.vm.yti.taxgen.sqliteprovider.writers.DbDimensions
import fi.vm.yti.taxgen.sqliteprovider.writers.DbDomains
import fi.vm.yti.taxgen.sqliteprovider.writers.DbHierarchies
import fi.vm.yti.taxgen.sqliteprovider.writers.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.writers.DbMetric
import fi.vm.yti.taxgen.sqliteprovider.writers.DbOwners
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
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

        initDbFileFromSeed(targetDbPath)
    }

    fun writeDpmDb(dpmDictionaries: List<DpmDictionary>) {
        diagnosticContext.withContext(
            contextType = DiagnosticContextType.WriteSQLiteDb,
            contextIdentifier = targetDbPath.toString()
        ) {
            connectDatabase()
            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            dpmDictionaries.forEach {
                writeDpmDictionary(it, languageIds)
            }
        }
    }

    private fun initDbFileFromSeed(targetDbPath: Path) {
        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, targetDbPath, StandardCopyOption.REPLACE_EXISTING)
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

        val dpmDictionaryItem = DpmDictionaryItem(
            dpmDictionary.owner,
            ownerId,
            languageIds
        )

        val domainItems = dpmDictionary.explicitDomains.map { explicitDomain ->

            val (domainId, memberIds) = DbDomains.writeExplicitDomainAndMembers(
                dpmDictionaryItem,
                explicitDomain
            )

            val hierarchyItems = explicitDomain.hierarchies.map { hierarchy ->
                val hierarchyId = DbHierarchies.writeHierarchyAndAndNodes(
                    dpmDictionaryItem,
                    hierarchy,
                    domainId,
                    memberIds
                )

                HierarchyItem(
                    hierarchyCode = hierarchy.hierarchyCode,
                    hierarchyId = hierarchyId
                )
            }

            DomainItem(
                domainCode = explicitDomain.domainCode,
                domainId = domainId,
                hierarchyItems = hierarchyItems
            )
        }

        dpmDictionaryItem.setDomainItems(domainItems)

        dpmDictionary.typedDomains.forEach { typedDomain ->
            DbDomains.writeTypedDomain(
                dpmDictionaryItem,
                typedDomain
            )
        }

        dpmDictionary.explicitDimensions.forEach { explicitDimension ->
            DbDimensions.writeExplicitDimension(
                dpmDictionaryItem,
                explicitDimension
            )
        }

        dpmDictionary.typedDimensions.forEach { typedDimension ->
            DbDimensions.writeTypedDimension(
                dpmDictionaryItem,
                typedDimension
            )
        }

        //TODO - insert "Open" member  with ID 9999

        dpmDictionary.metricDomains.map { metricDomain ->

            //TODO - Metric members IDs should start from 10000
            val (domainId, memberIds) = DbMetric.writeMetricDomainMembers(
                dpmDictionaryItem,
                metricDomain
            )

            metricDomain.hierarchies.map { hierarchy ->
                DbHierarchies.writeHierarchyAndAndNodes(
                    dpmDictionaryItem,
                    hierarchy,
                    domainId,
                    memberIds
                )
            }
        }
    }
}
