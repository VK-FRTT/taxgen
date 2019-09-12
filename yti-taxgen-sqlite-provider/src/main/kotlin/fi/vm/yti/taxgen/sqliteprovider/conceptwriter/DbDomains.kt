package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.DpmModelOptions
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        domain: ExplicitDomain,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        modelOptions: Map<DpmModelOptions, Any>,
        diagnostic: Diagnostic
    ): EntityID<Int> {

        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain,
                ownerId,
                languageIds,
                modelOptions,
                diagnostic
            )

            val domainId = insertExplicitDomain(
                domain,
                domainConceptId,
                owner
            )

            domain.members.forEach { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    member,
                    ownerId,
                    languageIds,
                    modelOptions,
                    diagnostic
                )

                insertMember(
                    domain,
                    domainId,
                    member,
                    memberConceptId,
                    owner
                )
            }

            domainId
        }
    }

    fun writeTypedDomain(
        domain: TypedDomain,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        modelOptions: Map<DpmModelOptions, Any>,
        diagnostic: Diagnostic
    ): EntityID<Int> {
        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain,
                ownerId,
                languageIds,
                modelOptions,
                diagnostic
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
            it[domainLabelCol] = domain.concept.label.defaultTranslationOrNull()
            it[domainDescriptionCol] = domain.concept.description.defaultTranslationOrNull()
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
            it[domainLabelCol] = domain.concept.label.defaultTranslationOrNull()
            it[domainDescriptionCol] = domain.concept.description.defaultTranslationOrNull()
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
    ) {
        val memberXbrlCode = "${owner.prefix}_${domain.domainCode}:${member.memberCode}"

        MemberTable.insert {
            it[memberCodeCol] = member.memberCode
            it[memberLabelCol] = member.concept.label.defaultTranslationOrNull()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = member.defaultMember
            it[conceptIdCol] = memberConceptId
            it[domainIdCol] = domainId
        }
    }
}
