package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

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
}
