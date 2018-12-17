package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

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
    val domainIdCol = reference("DomainID", DomainTable, ReferenceOption.NO_ACTION).nullable()

    // Longer description (English)
    val hierarchyDescriptionCol = text("HierarchyDescription").nullable()

    // Reference to concept (change, owner and translation) information
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
