package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mMetric` (
 *   `MetricID` INTEGER,
 *   `CorrespondingMemberID` INTEGER,
 *   `DataType` TEXT,
 *   `FlowType` TEXT,
 *   `BalanceType` TEXT,
 *   `ReferencedDomainID` INTEGER,
 *   `ReferencedHierarchyID` INTEGER,
 *   `HierarchyStartingMemberID` INTEGER,
 *   `IsStartingMemberIncluded` BOOLEAN,
 *   FOREIGN KEY(`HierarchyStartingMemberID`) REFERENCES `mMember`(`MemberID`),
 *   FOREIGN KEY(`ReferencedHierarchyID`) REFERENCES `mHierarchy`(`HierarchyID`),
 *   FOREIGN KEY(`ReferencedDomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`MetricID`),
 *   FOREIGN KEY(`CorrespondingMemberID`) REFERENCES `mMember`(`MemberID`)
 * );
 *
 * DataType reference values (from BR-AG Data Modeler):
 * - `String`
 * - `Percent`
 * - `Monetary`
 * - `Lei`
 * - `Isin`
 * - `Integer`
 * - `Enumeration/Code`
 * - `Decimal`
 * - `Date`
 * - `Boolean`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - TODO
 */
object MetricTable : IntIdTable(name = "mMetric", columnName = "MetricID") {
    val correspondingMemberCol = reference("CorrespondingMemberID", MemberTable, ReferenceOption.NO_ACTION).nullable()
    val dataTypeCol = text("DataType")
    val flowTypeCol = text("FlowType").nullable()
    val balanceTypeCol = text("BalanceType").nullable()
    val referencedDomainCol = reference("ReferencedDomainID", DomainTable, ReferenceOption.NO_ACTION).nullable()
    val referencedHierarchyCol = reference("ReferencedHierarchyID", HierarchyTable, ReferenceOption.NO_ACTION).nullable()
    val hierarchyStartingMemberCol = reference("HierarchyStartingMemberID", MemberTable, ReferenceOption.NO_ACTION).nullable()
    val isStartingMemberIncludedCol = bool("IsStartingMemberIncluded").nullable()
}
