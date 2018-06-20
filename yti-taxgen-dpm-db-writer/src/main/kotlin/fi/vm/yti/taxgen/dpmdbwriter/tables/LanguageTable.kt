package fi.vm.yti.taxgen.dpmdbwriter.tables

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
object LanguageTable : IntIdTable("mLanguage", "LanguageID") {
    val languageName = text("LanguageName").nullable()
    val englishName = text("EnglishName").nullable()
    val isoCode = text("IsoCode").nullable()
    val conceptID = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
