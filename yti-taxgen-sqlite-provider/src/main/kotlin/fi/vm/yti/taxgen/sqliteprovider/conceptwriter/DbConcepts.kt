package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTime
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTimeOrNull
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

object DbConcepts {

    private val requiredLabelLang by lazy { Language.byIso6391CodeOrFail("en") }
    private val fallbackCandidateLangs by lazy {
        listOf(
            Language.byIso6391CodeOrFail("fi"),
            Language.byIso6391CodeOrFail("sv")
        )
    }

    fun writeConceptAndTranslations(
        concept: Concept,
        conceptType: ConceptType,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>
    ): EntityID<Int> {

        val conceptId = insertConcept(
            concept,
            conceptType,
            ownerId
        )

        concept.label.translations.forEach { (language, text) ->
            insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.LABEL,
                language,
                text
            )
        }

        val fallbackTranslation = selectFallbackTranslationTextOrNull(
            concept.label,
            requiredLabelLang,
            fallbackCandidateLangs
        )

        if (fallbackTranslation != null) {
            insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.LABEL,
                requiredLabelLang,
                fallbackTranslation
            )
        }

        concept.description.translations.forEach { (language, text) ->
            insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.DESCRIPTION,
                language,
                text
            )
        }

        return conceptId
    }

    fun deleteAllConceptsAndTranslations(conceptType: ConceptType) {
        val conceptTypeString = conceptType.value

        val matchingConceptIds = ConceptTable
            .select { ConceptTable.conceptTypeCol eq conceptTypeString }
            .map { it[ConceptTable.id] }

        ConceptTranslationTable.deleteWhere { ConceptTranslationTable.conceptIdCol inList matchingConceptIds }
        ConceptTable.deleteWhere { ConceptTable.conceptTypeCol eq conceptTypeString }
    }

    private fun selectFallbackTranslationTextOrNull(
        translatedText: TranslatedText,
        requiredLang: Language,
        fallbackCandidateLangs: List<Language>
    ): String? {
        if (translatedText.translations.containsKey(requiredLang)) {
            return null
        }

        fallbackCandidateLangs.forEach { candidateLang ->
            val text = translatedText.translations[candidateLang]
            if (text != null) return text
        }

        return null
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
        languageIds: Map<Language, EntityID<Int>>,
        conceptId: EntityID<Int>,
        role: ConceptTranslationRole,
        language: Language,
        text: String
    ) {
        val languageId = languageIds[language] ?: thisShouldNeverHappen("Language without DB mapping: $language")

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = role.value
        }
    }
}
