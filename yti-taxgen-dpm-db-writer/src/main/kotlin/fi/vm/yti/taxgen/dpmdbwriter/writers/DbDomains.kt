package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.dpmdbwriter.DbWriteContext
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import fi.vm.yti.taxgen.dpmdbwriter.tables.DomainTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        writeContext: DbWriteContext,
        domain: ExplicitDomain
    ) {

        transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                writeContext,
                domain.concept,
                ConceptType.DOMAIN
            )

            val domainId = insertExplicitDomain(
                writeContext,
                domain,
                domainConceptId
            )

            domain.members.forEach { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    writeContext,
                    member.concept,
                    ConceptType.MEMBER
                )

                insertMember(
                    writeContext,
                    domain,
                    domainId,
                    member,
                    memberConceptId
                )
            }
        }
    }

    private fun insertExplicitDomain(
        writeContext: DbWriteContext,
        domain: ExplicitDomain,
        domainConceptId: EntityID<Int>
    ): EntityID<Int> {
        val domainXbrlCode = "${writeContext.owner.prefix}_exp:${domain.domainCode}"

        val domainId = DomainTable.insertAndGetId {
            it[domainCodeCol] = domain.domainCode
            it[domainLabelCol] = domain.concept.label.defaultText()
            it[domainDescriptionCol] = domain.concept.description.defaultText()
            it[domainXBRLCodeCol] = domainXbrlCode
            it[dataTypeCol] = null
            it[isTypedDomainCol] = false
            it[conceptIdCol] = domainConceptId
        }

        return domainId
    }

    private fun insertMember(
        writeContext: DbWriteContext,
        domain: ExplicitDomain,
        domainId: EntityID<Int>,
        member: Member,
        memberConceptId: EntityID<Int>
    ) {
        val memberXbrlCode = "${writeContext.owner.prefix}_${domain.domainCode}:${member.memberCode}"

        MemberTable.insertAndGetId {
            it[memberCodeCol] = member.memberCode
            it[memberLabelCol] = member.concept.label.defaultText()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = member.defaultMember
            it[conceptIdCol] = memberConceptId
            it[domainIdCol] = domainId
        }
    }
}
