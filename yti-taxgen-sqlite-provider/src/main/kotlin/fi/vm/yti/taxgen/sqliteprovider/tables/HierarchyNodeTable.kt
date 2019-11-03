package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

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
    val hierarchyIdCol = reference(
        name = "HierarchyID",
        foreign = HierarchyTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable().primaryKey()

    //Member this node represents
    val memberIdCol = reference(
        name = "MemberID",
        foreign = MemberTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable().primaryKey()

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
    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    //Path from the root of the hierarchy to this node, only MemberIDs are listed (Tree structure information)
    val pathCol = varchar("Path", 3999).nullable()

    fun insertHierarchyNode(
        hierarchyNodeConceptId: EntityID<Int>,
        hierarchyId: EntityID<Int>,
        parentNode: HierarchyNode?,
        node: HierarchyNode,
        memberDomainId: EntityID<Int>,
        level: Int,
        order: Int,
        inherentTextLanguage: Language?
    ) {
        val memberRow = MemberTable.rowWhereDomainIdAndMemberCode(memberDomainId, node.referencedElementCode)
            ?: thisShouldNeverHappen("No Member matching CurrentNode.referencedElementCode: ${node.referencedElementCode}")

        val parentMemberRow =
            if (parentNode == null) {
                null
            } else {
                MemberTable.rowWhereDomainIdAndMemberCode(memberDomainId, parentNode.referencedElementCode)
                    ?: thisShouldNeverHappen("No Member matching ParentNode.referencedElementCode: ${parentNode.referencedElementCode}")
            }

        HierarchyNodeTable.insert {
            it[hierarchyIdCol] = hierarchyId
            it[memberIdCol] = memberRow[MemberTable.id]
            it[isAbstractCol] = node.abstract
            it[comparisonOperatorCol] = node.comparisonOperator
            it[unaryOperatorCol] = node.unaryOperator
            it[orderCol] = order
            it[levelCol] = level
            it[parentMemberID] = parentMemberRow?.get(MemberTable.id)?.value
            it[hierarchyNodeLabel] = node.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[conceptIdCol] = hierarchyNodeConceptId
            it[pathCol] = null
        }
    }

    fun rowWhereHierarchyIdAndMemberCode(hierarchyId: EntityID<Int>, memberCode: String) =
        (HierarchyNodeTable innerJoin MemberTable).select {
            HierarchyNodeTable.hierarchyIdCol.eq(hierarchyId) and MemberTable.memberCodeCol.eq(memberCode)
        }.firstOrNull()

    fun rowWhereHierarchyIdAndMemberId(hierarchyId: EntityID<Int>, memberId: EntityID<Int>) = select {
        HierarchyNodeTable.hierarchyIdCol.eq(hierarchyId) and HierarchyNodeTable.memberIdCol.eq(memberId)
    }.firstOrNull()
}
