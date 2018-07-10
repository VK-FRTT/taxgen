package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.dpmdbwriter.tables.CONCEPT_TRANSLATION_ROLE_LABEL
import fi.vm.yti.taxgen.dpmdbwriter.tables.CONCEPT_TYPE_LANGUAGE
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.LanguageTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object DbLanguages {

    fun writeLanguages(languages: List<Language>): Map<Language, EntityID<Int>> {
        val languageIds = writeLanguageRows(languages)

        writeLanguageLabelRows(languageIds)

        return languageIds
    }

    private fun writeLanguageRows(languages: List<Language>): Map<Language, EntityID<Int>> {
        val languageIdsList = transaction {

            languages.map { language ->
                val languageId = insertLanguage(language)
                Pair(language, languageId)
            }
        }

        return languageIdsList.toMap()
    }

    private fun writeLanguageLabelRows(languageIds: Map<Language, EntityID<Int>>) {
        transaction {

            languageIds.forEach { (language, languageEntityId) ->

                val languageConceptId = insertLanguageConcept()

                language.label.forEach { (labelLanguage, labelText) ->

                    insertLanguageConceptTranslation(
                        languageConceptId,
                        languageIds[labelLanguage]!!,
                        labelText
                    )
                }

                updateLanguageToReferConcept(languageEntityId, languageConceptId)
            }
        }
    }

    private fun insertLanguage(language: Language): EntityID<Int> {
        return LanguageTable.insertAndGetId {
            it[languageNameCol] = language.nativeLabel()
            it[englishNameCol] = language.englishLabel()
            it[isoCodeCol] = language.iso6391Code
            it[conceptIdCol] = null
        }
    }

    private fun insertLanguageConcept(): EntityID<Int> {
        return ConceptTable.insertAndGetId {
            it[conceptTypeCol] = CONCEPT_TYPE_LANGUAGE
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
            it[roleCol] = CONCEPT_TRANSLATION_ROLE_LABEL
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
