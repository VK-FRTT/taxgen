package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * Represents a node in a hierarchy of members, specifying how members relate to each other,
 * and can also define the aggregations from lower to upper levels in the hierarchy.
 *
 * Reference DDL (from BR-AG Data Modeler):
 *
 * CREATE TABLE `mHierarchyNode` (
 *   `HierarchyID` INTEGER,
 *   `MemberID` INTEGER,
 *   `IsAbstract` BOOLEAN,
 *   `ComparisonOperator` TEXT,
 *   `UnaryOperator` TEXT,
 *   `Order` INTEGER,
 *   `Level` INTEGER,
 *   `ParentMemberID` INTEGER,
 *   `HierarchyNodeLabel` TEXT,
 *   `ConceptID` INTEGER,
 *   `Path` varchar (3999),
 *   FOREIGN KEY(`MemberID`) REFERENCES `mMember`(`MemberID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`HierarchyID`,`MemberID`),
 *   FOREIGN KEY(`HierarchyID`) REFERENCES `mHierarchy`(`HierarchyID`)
 *   );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - T4U does not define columns: HierarchyNodeLabel(TEXT), ConceptID(INTEGER)
 * - T4U defines column: Path(TEXT)
 */
object HierarchyNodeTable : Table(name = "mHierarchyNode") {

    //Hierarchy to which this node belongs
    val hierarchyIdCol = reference("HierarchyID", HierarchyTable, ReferenceOption.NO_ACTION).nullable().primaryKey()

    //Member this node represents
    val memberIdCol = reference("MemberID", MemberTable, ReferenceOption.NO_ACTION).nullable().primaryKey()

    //
    val isAbstractCol = bool("IsAbstract").nullable()

    //Indicates the relationship between this node and the aggregation of its children
    val comparisonOperatorCol = text("ComparisonOperator").nullable()

    //Indicates the contribution of this node to the aggregation of its siblings
    val unaryOperatorCol = text("UnaryOperator").nullable()

    //Position of this node within its set of siblings, if any (Tree structure information)
    val orderCol = integer("Order").nullable()

    //Level of this node, lower level numbered nodes contain higher numbered ones,
    // i.e. lower levels are nearer the root (Tree structure information)
    val levelCol = integer("Level").nullable()

    //Indicates the parent of this node, if any - i.e. the level immediately above (Tree structure information)
    //i.e ParentMemberID REFERENCES HierarchyNode(MemberID)
    val parentMemberID = integer("ParentMemberID").nullable()

    //
    val hierarchyNodeLabel = text("HierarchyNodeLabel").nullable()

    //
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()

    //Path from the root of the hierarchy to this node, only MemberIDs are listed (Tree structure information)
    val pathCol = varchar("Path", 3999).nullable()
}
