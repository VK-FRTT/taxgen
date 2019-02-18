package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mAxisOrdinate` (
 *   `AxisID`	INTEGER,
 *   `OrdinateID`	INTEGER NOT NULL,
 *   `OrdinateLabel`	TEXT,
 *   `OrdinateCode`	TEXT,
 *   `IsDisplayBeforeChildren`	BOOLEAN,
 *   `IsAbstractHeader`	BOOLEAN,
 *   `IsRowKey`	BOOLEAN,
 *   `Level`	INTEGER,
 *   `Order`	INTEGER,
 *   `ParentOrdinateID`	INTEGER,
 *   `ConceptID`	INTEGER,
 *   `TypeOfKey`	TEXT,
 *   FOREIGN KEY(`ParentOrdinateID`) REFERENCES `mAxisOrdinate`(`OrdinateID`),
 *   PRIMARY KEY(`OrdinateID`),
 *   FOREIGN KEY(`AxisID`) REFERENCES `mAxis`(`AxisID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object AxisOrdinateTable : IntIdTable("mAxisOrdinate", "OrdinateID") { //NOTE: In reference DDL OrdinateID is a second column
    val axisIdCol = reference("AxisID", AxisTable, ReferenceOption.NO_ACTION).nullable()
    val ordinateLabelCol = text("OrdinateLabel").nullable()
    val ordinateCodeCol = text("OrdinateCode").nullable()
    val isDisplayBeforeChildrenCol = bool("IsDisplayBeforeChildren").nullable()
    val isAbstractHeaderCol = bool("IsAbstractHeader").nullable()
    val isRowKeyCol = bool("IsRowKey").nullable()
    val levelCol = integer("Level").nullable()
    val orderCol = integer("Order").nullable()
    val parentOrdinateIdCol = reference("ParentOrdinateID", AxisOrdinateTable, ReferenceOption.NO_ACTION).nullable()
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
    val typeOfKeyCol = text("TypeOfKey").nullable()
}
