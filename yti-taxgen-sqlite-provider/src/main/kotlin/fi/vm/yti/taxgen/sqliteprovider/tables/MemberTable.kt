package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mMember` (
 *   `MemberID` INTEGER,
 *   `DomainID` INTEGER,
 *   `MemberCode` TEXT,
 *   `MemberLabel` TEXT,
 *   `MemberXBRLCode` TEXT,
 *   `IsDefaultMember` BOOLEAN,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`DomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`MemberID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 *   );
 **
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object MemberTable : IntIdTable(name = "mMember", columnName = "MemberID") {
    val domainIdCol = reference("DomainID", DomainTable, ReferenceOption.NO_ACTION).nullable()
    val memberCodeCol = text("MemberCode").nullable()
    val memberLabelCol = text("MemberLabel").nullable()
    val memberXBRLCodeCol = text("MemberXBRLCode").nullable()
    val isDefaultMemberCol = bool("IsDefaultMember").nullable()
    val conceptIdCol = reference("ConceptID", ConceptTable, ReferenceOption.NO_ACTION).nullable()
}
