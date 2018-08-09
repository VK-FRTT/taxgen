package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.Language
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

        concept.label.translations.forEach { (language, text) ->
            insertConceptTranslation(
                writeContext,
                conceptId,
                ConceptTranslationRole.LABEL,
                language,
                text
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
        writeContext: DbWriteContext,
        conceptId: EntityID<Int>,
        role: ConceptTranslationRole,
        language: Language,
        text: String
    ) {
        val languageId =
            writeContext.languageIds[language] ?: thisShouldNeverHappen("Language without DB mapping: $language")

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = role.value
        }
    }
}
