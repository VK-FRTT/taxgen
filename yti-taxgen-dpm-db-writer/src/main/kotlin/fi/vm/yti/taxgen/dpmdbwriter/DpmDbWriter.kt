package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.dpmdbwriter.tables.Tables
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbDomains
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbLanguages
import fi.vm.yti.taxgen.dpmdbwriter.writers.DbOwners
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.nio.file.Path
import java.sql.Connection

class DpmDbWriter(
    rawTargetDbPath: Path,
    forceOverwrite: Boolean,
    private val diagnostic: Diagnostic
) {
    private val targetDbPath: Path = rawTargetDbPath.toAbsolutePath().normalize()

    init {
        FileOps.deleteConflictingTargetFileIfAllowed(targetDbPath, forceOverwrite)
        FileOps.failIfTargetFileExists(targetDbPath, diagnostic)
        FileOps.createIntermediateFolders(targetDbPath)
    }

    fun writeDpmDb(dpmDictionaries: List<DpmDictionary>) {
        diagnostic.withContext(
            contextType = "Writing",
            contextName = "DPM Database",
            contextRef = targetDbPath.toString()
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

        val writeContext = DpmDictionaryWriteContext(
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
