package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.DpmElementRef
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.dpmdbwriter.DpmDictionaryWriteContext
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptType
import fi.vm.yti.taxgen.dpmdbwriter.tables.DomainTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        writeContext: DpmDictionaryWriteContext,
        domain: ExplicitDomain
    ): Pair<EntityID<Int>, Map<DpmElementRef, EntityID<Int>>> {

        val ret = transaction {
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

            val memberIds = mutableMapOf<DpmElementRef, EntityID<Int>>()

            domain.members.forEach { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    writeContext,
                    member.concept,
                    ConceptType.MEMBER
                )

                val memberId = insertMember(
                    writeContext,
                    domain,
                    domainId,
                    member,
                    memberConceptId
                )

                memberIds[member.ref()] = memberId
            }

            Pair(domainId, memberIds)
        }

        return ret
    }

    private fun insertExplicitDomain(
        writeContext: DpmDictionaryWriteContext,
        domain: ExplicitDomain,
        domainConceptId: EntityID<Int>
    ): EntityID<Int> {
        val domainXbrlCode = "${writeContext.owner.prefix}_exp:${domain.domainCode}"

        val domainId = DomainTable.insertAndGetId {
            it[domainCodeCol] = domain.domainCode
            it[domainLabelCol] = domain.concept.label.defaultTranslation()
            it[domainDescriptionCol] = domain.concept.description.defaultTranslation()
            it[domainXBRLCodeCol] = domainXbrlCode
            it[dataTypeCol] = null
            it[isTypedDomainCol] = false
            it[conceptIdCol] = domainConceptId
        }

        return domainId
    }

    private fun insertMember(
        writeContext: DpmDictionaryWriteContext,
        domain: ExplicitDomain,
        domainId: EntityID<Int>,
        member: Member,
        memberConceptId: EntityID<Int>
    ): EntityID<Int> {
        val memberXbrlCode = "${writeContext.owner.prefix}_${domain.domainCode}:${member.memberCode}"

        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = member.memberCode
            it[memberLabelCol] = member.concept.label.defaultTranslation()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = member.defaultMember
            it[conceptIdCol] = memberConceptId
            it[domainIdCol] = domainId
        }

        return memberId
    }
}
