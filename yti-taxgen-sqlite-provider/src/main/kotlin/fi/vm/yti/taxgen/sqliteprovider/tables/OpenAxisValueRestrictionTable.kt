package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mOpenAxisValueRestriction` (
 *   `AxisID INTEGER,
 *   `HierarchyID` INTEGER,
 *   `HierarchyStartingMemberID` INTEGER,
 *   `IsStartingMemberIncluded`	BOOLEAN,
 *   FOREIGN KEY(`AxisID`) REFERENCES `mAxis`(`AxisID`),
 *   FOREIGN KEY(`HierarchyID`) REFERENCES `mHierarchy`(`HierarchyID`),
 *   PRIMARY KEY(`AxisID`,`HierarchyID`),
 *   FOREIGN KEY(`HierarchyStartingMemberID`) REFERENCES `mMember`(`MemberID`)
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - TODO
 */
object OpenAxisValueRestrictionTable : Table("mOpenAxisValueRestriction") {
    val axisIdCol = reference("AxisID", AxisTable, ReferenceOption.NO_ACTION).nullable().primaryKey()
    val hierarchyIdCol = reference("HierarchyID", HierarchyTable, ReferenceOption.NO_ACTION).nullable()
    val hierarchyStartingMemberIdCol =
        reference("HierarchyStartingMemberID", MemberTable, ReferenceOption.NO_ACTION).nullable()
    val isStartingMemberIncluded = bool("IsStartingMemberIncluded").nullable()
}
