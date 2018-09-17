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
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object OwnerTable : IntIdTable(name = "mOwner", columnName = "OwnerID") {
    val ownerNameCol = text("OwnerName").nullable()
    val ownerNamespaceCol = text("OwnerNamespace").nullable()
    val ownerLocationCol = text("OwnerLocation").nullable()
    val ownerPrefixCol = text("OwnerPrefix").nullable()
    val ownerCopyrightCol = text("OwnerCopyright").nullable()

    val parentOwnerIdCol = reference("ParentOwnerID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
