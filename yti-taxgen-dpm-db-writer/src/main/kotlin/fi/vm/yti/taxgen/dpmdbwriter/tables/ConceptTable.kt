package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

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
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U):
 * - None
 */
object ConceptTable : IntIdTable("mConcept", "ConceptID") {
    val conceptType = text("ConceptType").nullable()

    val owner = reference("OwnerID", OwnerTable, ReferenceOption.NO_ACTION).nullable()

    val creationDate = date("CreationDate").nullable()
    val modificationDate = date("ModificationDate").nullable()

    val fromDate = date("FromDate").nullable()
    val toDate = date("ToDate").nullable()
}
