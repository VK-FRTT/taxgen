package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object DbDomains {
    fun writeExplicitDomainAndMembers(
        domain: ExplicitDomain,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
    ): EntityID<Int> {

        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain,
                ownerId,
                languageIds
            )

            val domainId = DomainTable.insertExplicitDomain(
                domain,
                domainConceptId,
                owner,
                processingOptions.sqliteDbDpmElementInherentTextLanguage
            )

            domain.members.forEach { member ->

                val memberConceptId = DbConcepts.writeConceptAndTranslations(
                    member,
                    ownerId,
                    languageIds
                )

                MemberTable.insertMember(
                    domain,
                    domainId,
                    member,
                    memberConceptId,
                    owner,
                    processingOptions.sqliteDbDpmElementInherentTextLanguage
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
        processingOptions: ProcessingOptions
    ): EntityID<Int> {
        return transaction {
            val domainConceptId = DbConcepts.writeConceptAndTranslations(
                domain,
                ownerId,
                languageIds
            )

            DomainTable.insertTypedDomain(
                domain,
                domainConceptId,
                owner,
                processingOptions.sqliteDbDpmElementInherentTextLanguage
            )
        }
    }
}
