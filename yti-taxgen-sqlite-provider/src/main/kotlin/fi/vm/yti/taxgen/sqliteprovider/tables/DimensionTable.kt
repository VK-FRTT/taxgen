package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mDimension` (
 *   `DimensionID` INTEGER,
 *   `DimensionLabel` TEXT,
 *   `DimensionCode` TEXT,
 *   `DimensionDescription` TEXT,
 *   `DimensionXBRLCode` TEXT,
 *   `DomainID` INTEGER,
 *   `IsTypedDimension` BOOLEAN,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`DomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`DimensionID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 * );
 **
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - TODO
 */
object DimensionTable : IntIdTable(name = "mDimension", columnName = "DimensionID") {
    val dimensionLabelCol = text("DimensionLabel").nullable()
    val dimensionCodeCol = text("DimensionCode").nullable()
    val dimensionDescriptionCol = text("DimensionDescription").nullable()
    val dimensionXBRLCodeCol = text("DimensionXBRLCode").nullable()
    val domainIdCol = reference("DomainID", DomainTable, ReferenceOption.NO_ACTION).nullable()
    val isTypedDimensionCol = bool("IsTypedDimension").nullable()
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
