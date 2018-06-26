package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.dpmdbwriter.tables.Tables
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection

class DpmDbProducer(
    targetDbPath: Path,
    private val forceOverwrite: Boolean
) {
    private val targetDbPath = targetDbPath.toAbsolutePath().normalize()
    private val database = initializeTargetDatabase()

    private fun initializeTargetDatabase(): Database {
        ensureTargetFoldersExist()
        ensureTargetFileDoesNotExist()

        return connectDatabase().also { Tables.create() }
    }

    fun writedb() {
        /*
        transaction {
            val owner = Owner()

            OwnerTable.insert {
                it[ownerName] = owner.name
                it[ownerNamespace] = owner.namespace
                it[ownerLocation] = owner.location
                it[ownerPrefix] = owner.prefix
                it[ownerCopyright] = owner.copyright

                it[parentOwner] = null
                it[concept] = null
            }
        }
        */
    }

    private fun ensureTargetFoldersExist() = Files.createDirectories(targetDbPath.parent)

    private fun ensureTargetFileDoesNotExist() {
        if (forceOverwrite) Files.deleteIfExists(targetDbPath)

        require(!Files.exists(targetDbPath))
    }

    private fun targetSqliteDbUrl() = "jdbc:sqlite:$targetDbPath"

    private fun connectDatabase(): Database = Database.connect(targetSqliteDbUrl(), "org.sqlite.JDBC")
        .also { TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE }
}
