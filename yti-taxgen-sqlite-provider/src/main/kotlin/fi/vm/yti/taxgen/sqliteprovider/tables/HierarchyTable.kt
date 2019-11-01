package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

/**
 * Hierarchies specify how members relate to each other,
 * and can also define the aggregations from lower to upper levels in the hierarchy.
 *
 * Reference DDL (from BR-AG Data Modeler):
 *
 * CREATE TABLE `mHierarchy` (
 *   `HierarchyID` INTEGER,
 *   `HierarchyCode` TEXT,
 *   `HierarchyLabel` TEXT,
 *   `DomainID` INTEGER,
 *   `HierarchyDescription` TEXT,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`DomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`HierarchyID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 *   );
 *
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object HierarchyTable : IntIdTable(name = "mHierarchy", columnName = "HierarchyID") {

    // Short code
    val hierarchyCodeCol = text("HierarchyCode").nullable()

    // Descriptive label (English)
    val hierarchyLabelCol = text("HierarchyLabel").nullable()

    // Domain this hierarchy relates to
    val domainIdCol = reference(
        name = "DomainID",
        foreign = DomainTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    // Longer description (English)
    val hierarchyDescriptionCol = text("HierarchyDescription").nullable()

    // Reference to concept (change, owner and translation) information
    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun rowWhereHierarchyId(hierarchyId: EntityID<Int>) = select {
        HierarchyTable.id.eq(hierarchyId)
    }.firstOrNull()

    fun rowWhereDomainIdAndHierarchyCode(domainId: EntityID<Int>, hierachyCode: String) = select {
        HierarchyTable.domainIdCol.eq(domainId) and HierarchyTable.hierarchyCodeCol.eq(hierachyCode)
    }.firstOrNull()
}
