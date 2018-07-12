package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.TargetPathChecks
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.dpmdbwriter.tables.Tables
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbDomains
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbLanguages
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbOwners
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Path
import java.sql.Connection

class DpmDbWriter(
    targetDbPath: Path,
    private val forceOverwrite: Boolean
) {
    private val targetDbPath = targetDbPath.toAbsolutePath().normalize()
    private val database = initializeTargetDatabase()
    private val languageIds = DbLanguages.writeLanguages()

    private fun initializeTargetDatabase(): Database {
        TargetPathChecks.deleteConflictingTargetFileIfAllowed(targetDbPath, forceOverwrite)
        TargetPathChecks.failIfTargetFileExists(targetDbPath)
        TargetPathChecks.createIntermediateFolders(targetDbPath)

        val db = connectDatabase()
        Tables.create()
        return db
    }

    private fun targetSqliteDbUrl() = "jdbc:sqlite:$targetDbPath"

    private fun connectDatabase(): Database {
        val db = Database.connect(targetSqliteDbUrl(), "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        return db
    }

    fun writeDpmDictionaries(dpmDictionaries: List<DpmDictionary>) {
        dpmDictionaries.forEach {
            writeDpmDictionary(it)
        }
    }

    private fun writeDpmDictionary(dpmDictionary: DpmDictionary) {
        val ownerId = DbOwners.writeOwner(dpmDictionary.owner)

        val writeContext = DbWriteContext(
            dpmDictionary.owner,
            ownerId,
            languageIds
        )

        dpmDictionary.explicitDomains.forEach { explicitDomain ->

            DbDomains.writeExplicitDomainAndMembers(
                writeContext,
                explicitDomain
            )
        }
    }
}
