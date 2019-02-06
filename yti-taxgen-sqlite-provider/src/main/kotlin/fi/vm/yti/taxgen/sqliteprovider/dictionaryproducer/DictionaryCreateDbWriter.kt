package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.sqliteprovider.DpmDbWriter
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DbLanguages
import fi.vm.yti.taxgen.sqliteprovider.conceptwriter.DpmDictionaries
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection

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

            connectDatabase()
            DbLanguages.configureLanguages()
            val languageIds = DbLanguages.resolveLanguageIds()

            DpmDictionaries.writeDpmDictionaries(dpmDictionaries, languageIds)
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
}
