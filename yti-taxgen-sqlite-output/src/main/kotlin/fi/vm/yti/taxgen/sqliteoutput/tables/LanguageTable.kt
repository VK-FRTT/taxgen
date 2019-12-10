package fi.vm.yti.taxgen.sqliteoutput.tables

import fi.vm.yti.taxgen.dpmmodel.Language
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mLanguage` (
 *   `LanguageID` INTEGER,
 *   `LanguageName` TEXT,
 *   `EnglishName` TEXT,
 *   `IsoCode` TEXT,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`LanguageID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U):
 * - None
 */
object LanguageTable : IntIdTable(name = "mLanguage", columnName = "LanguageID") {

    val languageNameCol = text("LanguageName").nullable()

    val englishNameCol = text("EnglishName").nullable()

    val isoCodeCol = text("IsoCode").nullable()

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun updateLanguageName(languageEntityId: EntityID<Int>, language: Language) {
        LanguageTable.update({ LanguageTable.id.eq(languageEntityId) }) {
            it[languageNameCol] = language.label.translations[language]
            it[englishNameCol] = language.label.translationForIso6391CodeOrNull("en")
        }
    }

    fun insertLanguage(language: Language) {
        LanguageTable.insert {
            it[languageNameCol] = language.label.translations[language]
            it[englishNameCol] = language.label.translationForIso6391CodeOrNull("en")
            it[isoCodeCol] = language.iso6391Code
            it[conceptIdCol] = null
        }
    }
}
