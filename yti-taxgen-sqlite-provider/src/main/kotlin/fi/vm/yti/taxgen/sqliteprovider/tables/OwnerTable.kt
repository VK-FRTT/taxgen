package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.dpmmodel.Owner
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

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

    val parentOwnerIdCol = reference(
        name = "ParentOwnerID",
        foreign = OwnerTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun insertOwner(
        owner: Owner
    ): EntityID<Int> {

        return OwnerTable.insertAndGetId {
            it[ownerNameCol] = owner.name
            it[ownerNamespaceCol] = owner.namespace
            it[ownerLocationCol] = owner.location
            it[ownerPrefixCol] = owner.prefix
            it[ownerCopyrightCol] = owner.copyright
            it[parentOwnerIdCol] = null
            it[conceptIdCol] = null
        }
    }

    fun rowsWhereOwnerPrefix(ownerPrefix: String) = select {
        OwnerTable.ownerPrefixCol eq ownerPrefix
    }
}
