package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.dpmdbwriter.DbWriteContext
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.joda.time.DateTime

object DbConcepts {

    fun writeConceptAndTranslations(
        writeContext: DbWriteContext,
        concept: Concept,
        conceptType: ConceptType
    ): EntityID<Int> {

        val conceptId = insertConcept(
            concept,
            conceptType,
            writeContext.ownerId
        )

        writeContext.ownerLanguageIds().forEach { (language, languageId) ->
            val labelText = concept.label.translations[language]

            insertConceptTranslation(
                conceptId,
                languageId,
                ConceptTranslationRole.LABEL,
                labelText
            )
        }

        return conceptId
    }

    private fun insertConcept(
        concept: Concept,
        conceptType: ConceptType,
        ownerId: EntityID<Int>
    ): EntityID<Int> {

        return ConceptTable.insertAndGetId {
            it[conceptTypeCol] = conceptType.value
            it[ownerIdCol] = ownerId
            it[creationDateCol] =
                instantToJodaDateTime(concept.createdAt)
            it[modificationDateCol] =
                instantToJodaDateTime(concept.modifiedAt)
            it[fromDateCol] =
                localDateToJodaDateTime(concept.applicableFrom)
            it[toDateCol] =
                localDateToJodaDateTime(concept.applicableUntil)
        }
    }

    private fun instantToJodaDateTime(instant: java.time.Instant): org.joda.time.DateTime {
        return DateTime(instant.toEpochMilli())
    }

    private fun localDateToJodaDateTime(localDate: java.time.LocalDate?): org.joda.time.DateTime? {
        localDate ?: return null
        return DateTime(localDate.toEpochDay())
    }

    private fun insertConceptTranslation(
        conceptId: EntityID<Int>,
        languageId: EntityID<Int>,
        role: ConceptTranslationRole,
        text: String?
    ) {

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = role.value
        }
    }
}
