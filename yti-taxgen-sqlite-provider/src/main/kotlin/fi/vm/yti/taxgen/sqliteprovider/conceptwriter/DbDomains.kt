package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.MemberLookupItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        domain: ExplicitDomain,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>
    ): Pair<EntityID<Int>, List<MemberLookupItem>> {

        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain.concept,
                ConceptType.DOMAIN,
                ownerId,
                languageIds
            )

            val domainId = insertExplicitDomain(
                domain,
                domainConceptId,
                owner
            )

            val memberLookupItems = domain.members.map { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    member.concept,
                    ConceptType.MEMBER,
                    ownerId,
                    languageIds
                )

                val (memberId, memberXbrlCode) = insertMember(
                    domain,
                    domainId,
                    member,
                    memberConceptId,
                    owner
                )

                MemberLookupItem(
                    memberUri = member.uri,
                    memberXbrlCode = memberXbrlCode,
                    defaultLabelText = member.concept.label.defaultTranslation(),
                    memberId = memberId
                )
            }

            Pair(domainId, memberLookupItems)
        }
    }

    fun writeTypedDomain(
        domain: TypedDomain,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>
    ): EntityID<Int> {
        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain.concept,
                ConceptType.DOMAIN,
                ownerId,
                languageIds
            )

            insertTypedDomain(
                domain,
                domainConceptId,
                owner
            )
        }
    }

    private fun insertExplicitDomain(
        domain: ExplicitDomain,
        domainConceptId: EntityID<Int>,
        owner: Owner
    ): EntityID<Int> {
        val domainXbrlCode = "${owner.prefix}_exp:${domain.domainCode}"

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
        domain: TypedDomain,
        domainConceptId: EntityID<Int>,
        owner: Owner
    ): EntityID<Int> {
        val domainXbrlCode = "${owner.prefix}_typ:${domain.domainCode}"

        return DomainTable.insertAndGetId {
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
        domain: ExplicitDomain,
        domainId: EntityID<Int>,
        member: Member,
        memberConceptId: EntityID<Int>,
        owner: Owner
    ): Pair<EntityID<Int>, String> {
        val memberXbrlCode = "${owner.prefix}_${domain.domainCode}:${member.memberCode}"

        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = member.memberCode
            it[memberLabelCol] = member.concept.label.defaultTranslation()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = member.defaultMember
            it[conceptIdCol] = memberConceptId
            it[domainIdCol] = domainId
        }

        return Pair(memberId, memberXbrlCode)
    }
}
