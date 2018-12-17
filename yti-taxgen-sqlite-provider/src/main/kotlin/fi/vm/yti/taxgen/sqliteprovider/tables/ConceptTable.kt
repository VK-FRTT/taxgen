package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

enum class ConceptType(val value: String) {
    LANGUAGE("Language"),
    DOMAIN("Domain"),
    MEMBER("Member"),
    HIERARCHY("Hierarchy"),
    HIERARCHY_NODE("HierarchyNode")
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
object ConceptTable : IntIdTable("mConcept", "ConceptID") {
    val conceptTypeCol = text("ConceptType").nullable()

    val ownerIdCol = reference("OwnerID", OwnerTable, ReferenceOption.NO_ACTION).nullable()

    val creationDateCol = sqliteDateTime("CreationDate").nullable()
    val modificationDateCol = sqliteDateTime("ModificationDate").nullable()

    val fromDateCol = sqliteDate("FromDate").nullable()
    val toDateCol = sqliteDate("ToDate").nullable()
}
