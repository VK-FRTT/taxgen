package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mMember` (
 *   `MemberID`	INTEGER,
 *   `DomainID`	INTEGER,
 *   `MemberCode`	TEXT,
 *   `MemberLabel`	TEXT,
 *   `MemberXBRLCode`	TEXT,
 *   `IsDefaultMember`	BOOLEAN,
 *   `ConceptID`	INTEGER,
 *   FOREIGN KEY(`DomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`MemberID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 *   );
 *
 * DataType reference values (from BR-AG Data Modeler):
 * - `String`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object MemberTable : IntIdTable(name = "mMember", columnName = "MemberID") {
    val memberCodeCol = text("MemberCode").nullable()
    val memberLabelCol = text("MemberLabel").nullable()
    val memberXBRLCodeCol = text("MemberXBRLCode").nullable()
    val isDefaultMemberCol = bool("IsDefaultMember").nullable()
    val conceptIdCol = reference("ConceptID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
    val domainIdCol = reference("DomainID", OwnerTable, ReferenceOption.NO_ACTION).nullable()
}
