package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import fi.vm.yti.taxgen.dpmdbwriter.tables.LanguageTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object DbLanguages {

    fun writeLanguages(): Map<Language, EntityID<Int>> {
        val languages = Language.languages()

        val languageIds = writeLanguages(languages)

        writeLanguageLabels(languageIds)

        return languageIds
    }

    private fun writeLanguages(languages: Set<Language>): Map<Language, EntityID<Int>> {
        val languageIdsList = transaction {

            languages.map { Pair(it, insertLanguage(it)) }
        }

        return languageIdsList.toMap()
    }

    private fun writeLanguageLabels(languageIds: Map<Language, EntityID<Int>>) {
        transaction {

            languageIds.forEach { (language, languageEntityId) ->

                val languageConceptId = insertLanguageConcept()

                language.label.translations.forEach { (translationLanguage, text) ->

                    insertLanguageConceptTranslation(
                        languageConceptId,
                        languageIds[translationLanguage]!!,
                        text
                    )
                }

                updateLanguageToReferConcept(
                    languageEntityId,
                    languageConceptId
                )
            }
        }
    }

    private fun insertLanguage(language: Language): EntityID<Int> {
        val nativeLanguageName = language.label.translations[language]

        return LanguageTable.insertAndGetId {
            it[languageNameCol] = nativeLanguageName
            it[englishNameCol] = language.label.defaultTranslation()
            it[isoCodeCol] = language.iso6391Code
            it[conceptIdCol] = null
        }
    }

    private fun insertLanguageConcept(): EntityID<Int> {
        return ConceptTable.insertAndGetId {
            it[conceptTypeCol] = ConceptType.LANGUAGE.value
            it[ownerIdCol] = null
            it[creationDateCol] = null
            it[modificationDateCol] = null
            it[fromDateCol] = null
            it[toDateCol] = null
        }
    }

    private fun insertLanguageConceptTranslation(
        conceptId: EntityID<Int>,
        languageId: EntityID<Int>,
        text: String
    ) {
        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = ConceptTranslationRole.LABEL.value
        }
    }

    private fun updateLanguageToReferConcept(
        languageEntityId: EntityID<Int>,
        languageConceptId: EntityID<Int>
    ) {
        LanguageTable.update({ LanguageTable.id.eq(languageEntityId) }) {
            it[conceptIdCol] = languageConceptId
        }
    }
}