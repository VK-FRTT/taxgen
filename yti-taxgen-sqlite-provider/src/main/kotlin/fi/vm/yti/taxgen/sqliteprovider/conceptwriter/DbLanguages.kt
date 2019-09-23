package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.tables.LanguageTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

object DbLanguages {

    fun configureLanguages() {
        transaction {
            val existingIsoCodes = LanguageTable
                .selectAll()
                .onEach { resultRow ->
                    val rowId = resultRow[LanguageTable.id]
                    val rowIsoCode = resultRow[LanguageTable.isoCodeCol]!!
                    tryUpdateExistingLanguage(rowId, rowIsoCode)
                }.map { resultRow ->
                    resultRow[LanguageTable.isoCodeCol]!!
                }

            Language.languages()
                .filterNot { existingIsoCodes.contains(it.iso6391Code) }
                .forEach {
                    insertLanguage(it)
                }
        }
    }

    fun resolveLanguageIds(): Map<Language, EntityID<Int>> {
        val languageIds = transaction {

            Language.languages()
                .map { language ->
                    val row = LanguageTable.select { LanguageTable.isoCodeCol eq language.iso6391Code }.first()
                    language to row[LanguageTable.id]
                }
                .toMap()
        }

        return languageIds
    }

    private fun tryUpdateExistingLanguage(languageEntityId: EntityID<Int>, languageIsoCode: String) {
        Language.languages()
            .find { it.iso6391Code == languageIsoCode }
            ?.let { language ->
                LanguageTable.update({ LanguageTable.id.eq(languageEntityId) }) {
                    it[languageNameCol] = language.label.translations[language]
                    it[englishNameCol] = language.label.translationForIso6391CodeOrNull("en")
                }
            }
    }

    private fun insertLanguage(language: Language) {
        LanguageTable.insert {
            it[languageNameCol] = language.label.translations[language]
            it[englishNameCol] = language.label.translationForIso6391CodeOrNull("en")
            it[isoCodeCol] = language.iso6391Code
            it[conceptIdCol] = null
        }
    }
}
