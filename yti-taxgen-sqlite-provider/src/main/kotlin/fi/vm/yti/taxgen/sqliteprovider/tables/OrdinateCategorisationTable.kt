package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mOrdinateCategorisation` (
 *   `OrdinateID` INTEGER,
 *   `DimensionID` INTEGER,
 *   `MemberID` INTEGER,
 *   `DimensionMemberSignature` TEXT,
 *   `Source` Text,
 *   `DPS` TEXT,
 *   FOREIGN KEY(`OrdinateID`) REFERENCES `mAxisOrdinate`(`OrdinateID`),
 *   FOREIGN KEY(`MemberID`) REFERENCES `mMember`(`MemberID`),
 *   FOREIGN KEY(`DimensionID`) REFERENCES `mDimension`(`DimensionID`),
 *   PRIMARY KEY(`OrdinateID`,`DimensionID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object OrdinateCategorisationTable : Table("mOrdinateCategorisation") {
    val ordinateIdCol: Column<EntityID<Int>?> = reference("OrdinateID", AxisOrdinateTable, ReferenceOption.NO_ACTION).nullable().primaryKey()
    val dimensionIdCol: Column<EntityID<Int>?> = reference("DimensionID", DimensionTable, ReferenceOption.NO_ACTION).nullable().primaryKey()
    val memberIdCol: Column<EntityID<Int>?> = reference("MemberID", MemberTable, ReferenceOption.NO_ACTION).nullable()
    val dimensionMemberSignatureCol: Column<String?> = text("DimensionMemberSignature").nullable()
    val sourceCol = text("Source").nullable()
    val dpsCol = text("DPS").nullable()
}
