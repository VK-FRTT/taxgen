package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mOwner` (
 *   `OwnerID` INTEGER,
 *   `OwnerName` TEXT,
 *   `OwnerNamespace` TEXT,
 *   `OwnerLocation` TEXT,
 *   `OwnerPrefix` TEXT,
 *   `OwnerCopyright` TEXT,
 *   `ParentOwnerID` INTEGER,
 *   `ConceptID` INTEGER,
 *   PRIMARY KEY(`OwnerID`),
 *   FOREIGN KEY(`ParentOwnerID`) REFERENCES `mOwner`(`OwnerID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 *   );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U):
 * - None
 */
object OwnerTable : IntIdTable(name = "mOwner", columnName = "OwnerID") {
    val ownerName = text("OwnerName").nullable()
    val ownerNamespace = text("OwnerNamespace").nullable()
    val ownerLocation = text("OwnerLocation").nullable()
    val ownerPrefix = text("OwnerPrefix").nullable()
    val ownerCopyright = text("OwnerCopyright").nullable()

    val parentOwner = reference("ParentOwnerID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
    val concept = reference("ConceptID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
}
