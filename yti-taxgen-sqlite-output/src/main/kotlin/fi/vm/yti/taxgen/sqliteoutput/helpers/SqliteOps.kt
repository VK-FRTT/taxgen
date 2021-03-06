package fi.vm.yti.taxgen.sqliteoutput.helpers

import java.nio.file.Path
import java.sql.Connection
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager

internal object SqliteOps {

    fun connectDatabase(targetDbPath: Path) {
        val dbUrl = "jdbc:sqlite:$targetDbPath"
        Database.connect(dbUrl, "org.sqlite.JDBC")
        TransactionManager.resetCurrent(null)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    }
}
