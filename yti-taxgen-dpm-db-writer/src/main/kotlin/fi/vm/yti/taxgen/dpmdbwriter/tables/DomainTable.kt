package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mDomain` (
 *   `DomainID` INTEGER NOT NULL,
 *   `DomainCode` TEXT,
 *   `DomainLabel` TEXT,
 *   `DomainDescription` TEXT,
 *   `DomainXBRLCode` TEXT,
 *   `DataType` TEXT,
 *   `IsTypedDomain` BOOLEAN,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`DomainID`)
 * );
 *
 * DataType reference values (from BR-AG Data Modeler):
 * - `String`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - T4U defines DataType as INTEGER (most likely error in spec)
 */
object DomainTable : IntIdTable(name = "mDomain", columnName = "DomainID") {
    val domainCodeCol = text("DomainCode").nullable()
    val domainLabelCol = text("DomainLabel").nullable()
    val domainDescriptionCol = text("DomainDescription").nullable()
    val domainXBRLCodeCol = text("DomainXBRLCode").nullable()
    val dataTypeCol = text("DataType").nullable()
    val isTypedDomainCol = bool("IsTypedDomain").nullable()
    val conceptIdCol = reference("ConceptID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
}
