package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Language
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

enum class ConceptTranslationRole(val value: String) {
    LABEL("label"),
    DESCRIPTION("description")
}

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mConceptTranslation` (
 *   `ConceptID` INTEGER,
 *   `LanguageID` INTEGER,
 *   `Text` TEXT,
 *   `Role` TEXT,
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`ConceptID`,`LanguageID`,`Role`),
 *   FOREIGN KEY(`LanguageID`) REFERENCES `mLanguage`(`LanguageID`)
 * );
 *
 * Role reference values (from BR-AG Data Modeler):
 * - `label`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - T4U defines additional column: OwnerID(INTEGER)
 * - T4U does not define column: Role(TEXT)
 */
object ConceptTranslationTable : Table(name = "mConceptTranslation") {

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable().primaryKey()

    val languageIdCol = reference(
        name = "LanguageID",
        foreign = LanguageTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable().primaryKey()

    val textCol = text("Text").nullable()

    val roleCol = text("Role").nullable().primaryKey()

    fun insertConceptTranslation(
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
