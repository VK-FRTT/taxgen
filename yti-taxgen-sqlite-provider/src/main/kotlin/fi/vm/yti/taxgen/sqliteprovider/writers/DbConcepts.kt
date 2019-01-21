package fi.vm.yti.taxgen.sqliteprovider.writers

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTime
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTimeOrNull
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId

object DbConcepts {

    private val requiredLabelLang by lazy { Language.byIso6391CodeOrFail("en") }
    private val fallbackCandidateLangs by lazy {
        listOf(
            Language.byIso6391CodeOrFail("fi"),
            Language.byIso6391CodeOrFail("sv")
        )
    }

    fun writeConceptAndTranslations(
        dictionaryItem: DpmDictionaryItem,
        concept: Concept,
        conceptType: ConceptType
    ): EntityID<Int> {

        val conceptId = insertConcept(
            concept,
            conceptType,
            dictionaryItem.ownerId
        )

        concept.label.translations.forEach { (language, text) ->
            insertConceptTranslation(
                dictionaryItem,
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
                dictionaryItem,
                conceptId,
                ConceptTranslationRole.LABEL,
                requiredLabelLang,
                fallbackTranslation
            )
        }

        concept.description.translations.forEach { (language, text) ->
            insertConceptTranslation(
                dictionaryItem,
                conceptId,
                ConceptTranslationRole.DESCRIPTION,
                language,
                text
            )
        }

        return conceptId
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
        dictionaryItem: DpmDictionaryItem,
        conceptId: EntityID<Int>,
        role: ConceptTranslationRole,
        language: Language,
        text: String
    ) {
        val languageId =
            dictionaryItem.languageIds[language] ?: thisShouldNeverHappen("Language without DB mapping: $language")

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = role.value
        }
    }
}
