package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.dpmdbwriter.DbWriteContext
import fi.vm.yti.taxgen.dpmdbwriter.ext.java.toJodaDateTime
import fi.vm.yti.taxgen.dpmdbwriter.ext.java.toJodaDateTimeOrNull
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId

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

            //TODO - Should null labels be inserted to DB ?
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
            it[creationDateCol] = concept.createdAt.toJodaDateTime()
            it[modificationDateCol] = concept.modifiedAt.toJodaDateTime()
            it[fromDateCol] = concept.applicableFrom.toJodaDateTimeOrNull()
            it[toDateCol] = concept.applicableUntil.toJodaDateTimeOrNull()
        }
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
