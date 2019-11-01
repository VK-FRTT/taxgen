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
 * - In T4U spec DataType is integer
 */
object MetricTable : IntIdTable(name = "mMetric", columnName = "MetricID") {

    val correspondingMemberCol = reference(
        name = "CorrespondingMemberID",
        foreign = MemberTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val dataTypeCol = text("DataType")

    val flowTypeCol = text("FlowType").nullable()

    val balanceTypeCol = text("BalanceType").nullable()

    val referencedDomainCol = reference(
        name = "ReferencedDomainID",
        foreign = DomainTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val referencedHierarchyCol = reference(
        name = "ReferencedHierarchyID",
        foreign = HierarchyTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val hierarchyStartingMemberCol = reference(
        name = "HierarchyStartingMemberID",
        foreign = MemberTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val isStartingMemberIncludedCol = bool("IsStartingMemberIncluded").nullable()
}
