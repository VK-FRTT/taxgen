package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTime
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTimeOrNull
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

enum class ConceptType(val value: String) {
    LANGUAGE("Language"),
    DOMAIN("Domain"),
    MEMBER("Member"),
    HIERARCHY("Hierarchy"),
    HIERARCHY_NODE("HierarchyNode"),
    DIMENSION("Dimension")
}

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mConcept` (
 *   `ConceptID` INTEGER,
 *   `ConceptType` TEXT,
 *   `OwnerID` INTEGER,
 *   `CreationDate` DATE,
 *   `ModificationDate` DATE,
 *   `FromDate` DATE,
 *   `ToDate` DATE,
 *   PRIMARY KEY(`ConceptID`),
 *   FOREIGN KEY(`OwnerID`) REFERENCES `mOwner`(`OwnerID`)
 * );
 *
 * ConceptType reference values (from BR-AG Data Modeler):
 * - `Domain`
 * - `Member`
 * - `Hierarchy`
 * - `HierarchyNode`
 * - `Dimension`
 * - `Framework`
 * - `Taxonomy`
 * - `TemplateOrTable`
 * - `Table`
 * - `Axis`
 * - `AxisOrdinate`
 * - `Module`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object ConceptTable : IntIdTable(name = "mConcept", columnName = "ConceptID") {

    val conceptTypeCol = text("ConceptType").nullable()

    val ownerIdCol = reference(
        name = "OwnerID",
        foreign = OwnerTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val creationDateCol = sqliteDateTime("CreationDate").nullable()

    val modificationDateCol = sqliteDateTime("ModificationDate").nullable()

    val fromDateCol = sqliteDate("FromDate").nullable()

    val toDateCol = sqliteDate("ToDate").nullable()

    fun insertConcept(
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

    fun deleteConceptsAndTranslationsOfType(conceptType: ConceptType) {

        ConceptTranslationTable.deleteWhere {
            ConceptTranslationTable.conceptIdCol inSubQuery
                ConceptTable.slice(ConceptTable.id).select { ConceptTable.conceptTypeCol eq conceptType.value }
        }

        ConceptTable.deleteWhere { ConceptTable.conceptTypeCol eq conceptType.value }
    }
}
