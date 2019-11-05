package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object DbMetric {

    fun writeMetricDomainMembers(
        metricDomain: MetricDomain,
        metricDomainId: EntityID<Int>,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions

    ) {
        transaction {

            metricDomain.metrics.forEach { metric ->

                val metricMemberConceptId = DbConcepts.writeConceptAndTranslations(
                    metric,
                    ownerId,
                    languageIds
                )

                val metricMemberId = MemberTable.insertMetricMember(
                    metric,
                    owner,
                    metricMemberConceptId,
                    metricDomainId,
                    processingOptions.sqliteDbDpmElementInherentTextLanguage
                )

                MetricTable.insertMetric(
                    metric,
                    metricMemberId,
                    owner
                )
            }
        }
    }
}
