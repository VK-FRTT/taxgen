package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteoutput.tables.LanguageTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DbLanguages {

    fun configureLanguages() {
        transaction {
            val existingIsoCodes = LanguageTable
                .slice(LanguageTable.id, LanguageTable.isoCodeCol)
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
                    LanguageTable.insertLanguage(it)
                }
        }
    }

    fun resolveLanguageIds(): Map<Language, EntityID<Int>> {
        val languageIsoCodesToId = transaction {
            LanguageTable
                .slice(LanguageTable.id, LanguageTable.isoCodeCol)
                .selectAll()
                .map { it[LanguageTable.isoCodeCol] to it[LanguageTable.id] }
                .toMap()
        }

        return Language.languages()
            .mapNotNull { language ->
                val entityID = languageIsoCodesToId[language.iso6391Code]

                if (entityID == null) {
                    null
                } else {
                    language to entityID
                }
            }
            .toMap()
    }

    private fun tryUpdateExistingLanguage(languageEntityId: EntityID<Int>, languageIsoCode: String) {
        Language.languages()
            .find { it.iso6391Code == languageIsoCode }
            ?.let { LanguageTable.updateLanguageName(languageEntityId, it) }
    }
}
