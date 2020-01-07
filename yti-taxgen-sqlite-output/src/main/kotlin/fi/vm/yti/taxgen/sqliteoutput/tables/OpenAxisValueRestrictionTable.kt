package fi.vm.yti.taxgen.sqliteoutput.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert

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
 * - None
 */
object OpenAxisValueRestrictionTable : Table("mOpenAxisValueRestriction") {

    val axisIdCol = reference(
        name = "AxisID",
        foreign = AxisTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val hierarchyIdCol = reference(
        name = "HierarchyID",
        foreign = HierarchyTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val hierarchyStartingMemberIdCol =
        reference(
            "HierarchyStartingMemberID",
            foreign = MemberTable,
            onDelete = ReferenceOption.NO_ACTION,
            onUpdate = ReferenceOption.NO_ACTION
        ).nullable()

    val isStartingMemberIncludedCol = bool("IsStartingMemberIncluded").nullable()

    override val primaryKey = PrimaryKey(axisIdCol)

    fun insertOpenAxisValueRestriction(
        axisId: EntityID<Int>?,
        hierarchyId: EntityID<Int>?,
        hierarchyStartingMemberId: EntityID<Int>?,
        isStartingMemberIncluded: Boolean?
    ) {
        OpenAxisValueRestrictionTable.insert {
            it[axisIdCol] = axisId
            it[hierarchyIdCol] = hierarchyId
            it[hierarchyStartingMemberIdCol] = hierarchyStartingMemberId
            it[isStartingMemberIncludedCol] = isStartingMemberIncluded
        }
    }
}
