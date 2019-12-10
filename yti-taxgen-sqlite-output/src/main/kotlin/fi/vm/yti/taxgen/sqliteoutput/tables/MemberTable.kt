package fi.vm.yti.taxgen.sqliteoutput.tables

import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.Owner
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

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

    val domainIdCol = reference(
        name = "DomainID",
        foreign = DomainTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val memberCodeCol = text("MemberCode").nullable()

    val memberLabelCol = text("MemberLabel").nullable()

    val memberXBRLCodeCol = text("MemberXBRLCode").nullable()

    val isDefaultMemberCol = bool("IsDefaultMember").nullable()

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun insertMember(
        domain: ExplicitDomain,
        domainId: EntityID<Int>,
        member: Member,
        memberConceptId: EntityID<Int>,
        owner: Owner,
        inherentTextLanguage: Language?
    ) {
        MemberTable.insert {
            it[memberCodeCol] = member.memberCode
            it[memberLabelCol] = member.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[memberXBRLCodeCol] = memberXbrlCode(owner, domain.domainCode, member.memberCode)
            it[isDefaultMemberCol] = member.defaultMember
            it[conceptIdCol] = memberConceptId
            it[domainIdCol] = domainId
        }
    }

    fun insertMetricMember(
        metric: Metric,
        owner: Owner,
        metricMemberConceptId: EntityID<Int>,
        metricDomainId: EntityID<Int>,
        inherentTextLanguage: Language?
    ): EntityID<Int> {
        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = metric.metricCode
            it[memberLabelCol] = metric.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[memberXBRLCodeCol] = metricMemberXbrlCode(owner, metric.metricCode)
            it[isDefaultMemberCol] = false
            it[conceptIdCol] = metricMemberConceptId
            it[domainIdCol] = metricDomainId
        }

        return memberId
    }

    fun rowWhereMemberId(memberId: EntityID<Int>) = select {
        MemberTable.id.eq(memberId)
    }.firstOrNull()

    fun rowWhereMemberXbrlCode(xbrlCode: String) = select {
        MemberTable.memberXBRLCodeCol.eq(xbrlCode)
    }.firstOrNull()

    fun rowWhereDomainIdAndMemberCode(domainId: EntityID<Int>, memberCode: String) = select {
        MemberTable.domainIdCol.eq(domainId) and MemberTable.memberCodeCol.eq(memberCode)
    }.firstOrNull()

    fun openMemberRow() = select {
        MemberTable.id.eq(9999)
    }.firstOrNull()

    fun memberXbrlCode(owner: Owner, domainCode: String, memberCode: String) = "${owner.prefix}_$domainCode:$memberCode"

    fun metricMemberXbrlCode(owner: Owner, metricCode: String) = "${owner.prefix}_met:$metricCode"
}
