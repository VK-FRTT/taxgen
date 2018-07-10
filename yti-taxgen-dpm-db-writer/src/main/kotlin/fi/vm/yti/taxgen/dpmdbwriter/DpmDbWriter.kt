package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.TargetPathChecks
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Languages
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.OwnerTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.nio.file.Path
import java.sql.Connection

class DpmDbWriter(
    targetDbPath: Path,
    private val forceOverwrite: Boolean
) {
    private val targetDbPath = targetDbPath.toAbsolutePath().normalize()
    private val database = initializeTargetDatabase()

    private fun initializeTargetDatabase(): Database {
        TargetPathChecks.deleteConflictingTargetFileIfAllowed(targetDbPath, forceOverwrite)
        TargetPathChecks.failIfTargetFileExists(targetDbPath)
        TargetPathChecks.createIntermediateFolders(targetDbPath)

        val db = connectDatabase()
        DbTables.create()

        val languages = Languages.languages()
        DbLanguages.writeLanguages(languages)

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

    fun writeDpmDictionary(dpmDictionary: DpmDictionary) {
        writeOwner(dpmDictionary.owner)

        dpmDictionary.explicitDomains.forEach { explicitDomain ->
            writeExplicitDomain(explicitDomain)
        }
    }

    fun writeExplicitDomain(explicitDomain: ExplicitDomain) {

        transaction {
            val explicitDomainConcept = explicitDomain.concept

            val conceptId = ConceptTable.insertAndGetId {
                it[conceptTypeCol] = "Domain"
                it[ownerIdCol] = null
                it[creationDateCol] = javaInstantToJodaDateTime(explicitDomainConcept.createdAt)
                it[modificationDateCol] = javaInstantToJodaDateTime(explicitDomainConcept.modifiedAt)
                it[fromDateCol] = javaLocalDateToJodaDateTime(explicitDomainConcept.applicableFrom)
                it[toDateCol] = javaLocalDateToJodaDateTime(explicitDomainConcept.applicableUntil)
            }

            explicitDomainConcept.label.translations.forEach { (langCode, text) ->
                val conceptId = ConceptTranslationTable.insert {
                    it[conceptIdCol] = conceptId
                    it[languageIdCol] = null
                    it[textCol] = text
                    it[roleCol] = "label"
                }
            }
        }
    }

    private fun writeOwner(owner: Owner) {
        transaction {
            val ownerId = OwnerTable.insertAndGetId {
                it[ownerNameCol] = owner.name
                it[ownerNamespaceCol] = owner.namespace
                it[ownerLocationCol] = owner.location
                it[ownerPrefixCol] = owner.prefix
                it[ownerCopyrightCol] = owner.copyright

                it[parentOwnerIdCol] = null
                it[conceptIdCol] = null
            }
        }
    }

    private fun javaInstantToJodaDateTime(instant: java.time.Instant): org.joda.time.DateTime {
        return DateTime(instant.toEpochMilli())
    }

    private fun javaLocalDateToJodaDateTime(localDate: java.time.LocalDate?): org.joda.time.DateTime? {
        localDate ?: return null
        return DateTime(localDate.toEpochDay())
    }
}
