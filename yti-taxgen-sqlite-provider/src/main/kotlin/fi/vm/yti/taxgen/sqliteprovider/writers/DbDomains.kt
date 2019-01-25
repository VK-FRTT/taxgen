package fi.vm.yti.taxgen.sqliteprovider.writers

import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        dictionaryItem: DpmDictionaryItem,
        domain: ExplicitDomain
    ): Pair<EntityID<Int>, Map<String, EntityID<Int>>> {

        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                domain.concept,
                ConceptType.DOMAIN
            )

            val domainId: EntityID<Int> = insertExplicitDomain(
                dictionaryItem,
                domain,
                domainConceptId
            )

            val memberIds = domain.members.map { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    dictionaryItem,
                    member.concept,
                    ConceptType.MEMBER
                )

                val memberId = insertMember(
                    dictionaryItem,
                    domain,
                    domainId,
                    member,
                    memberConceptId
                )

                member.uri to memberId
            }.toMap()

            Pair(domainId, memberIds)
        }
    }

    fun writeTypedDomain(
        dictionaryItem: DpmDictionaryItem,
        domain: TypedDomain
    ) {
        transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                domain.concept,
                ConceptType.DOMAIN
            )

            insertTypedDomain(
                dictionaryItem,
                domain,
                domainConceptId
            )
        }
    }

    private fun insertExplicitDomain(
        dictionaryItem: DpmDictionaryItem,
        domain: ExplicitDomain,
        domainConceptId: EntityID<Int>
    ): EntityID<Int> {
        val domainXbrlCode = "${dictionaryItem.owner.prefix}_exp:${domain.domainCode}"

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

    private fun insertTypedDomain(
        dictionaryItem: DpmDictionaryItem,
        domain: TypedDomain,
        domainConceptId: EntityID<Int>
    ) {
        val domainXbrlCode = "${dictionaryItem.owner.prefix}_typ:${domain.domainCode}"

        DomainTable.insert {
            it[domainCodeCol] = domain.domainCode
            it[domainLabelCol] = domain.concept.label.defaultTranslation()
            it[domainDescriptionCol] = domain.concept.description.defaultTranslation()
            it[domainXBRLCodeCol] = domainXbrlCode
            it[dataTypeCol] = domain.dataType
            it[isTypedDomainCol] = true
            it[conceptIdCol] = domainConceptId
        }
    }

    private fun insertMember(
        dictionaryItem: DpmDictionaryItem,
        domain: ExplicitDomain,
        domainId: EntityID<Int>,
        member: Member,
        memberConceptId: EntityID<Int>
    ): EntityID<Int> {
        val memberXbrlCode = "${dictionaryItem.owner.prefix}_${domain.domainCode}:${member.memberCode}"

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
