package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
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

                val metricMemberId = insertMetricMember(
                    metric,
                    owner,
                    metricMemberConceptId,
                    metricDomainId,
                    processingOptions
                )

                insertMetric(
                    metric,
                    metricMemberId
                )
            }
        }
    }

    private fun insertMetricMember(
        metric: Metric,
        owner: Owner,
        metricMemberConceptId: EntityID<Int>,
        metricDomainId: EntityID<Int>,
        processingOptions: ProcessingOptions
    ): EntityID<Int> {
        val memberXbrlCode = "${owner.prefix}_met:${metric.metricCode}"

        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = metric.metricCode
            it[memberLabelCol] = metric.concept.label.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = false
            it[conceptIdCol] = metricMemberConceptId
            it[domainIdCol] = metricDomainId
        }

        return memberId
    }

    private fun insertMetric(
        metric: Metric,
        metricMemberId: EntityID<Int>
    ) {
        val referencedRows = metric.referencedDomainCode?.let { referencedDomainCode ->

            val domainRow = DomainTable.rowWhereDomainCode(referencedDomainCode)
                ?: thisShouldNeverHappen("No Domain matching Metric.ReferencedDomainCode: $referencedDomainCode")

            val hierarchyRow = metric.referencedHierarchyCode?.let { referencedHierarchyCode ->
                HierarchyTable.rowWhereDomainIdAndHierarchyCode(domainRow[DomainTable.id], referencedHierarchyCode)
                    ?: thisShouldNeverHappen("No Hierarchy matching Metric.ReferencedHierarchyCode: $referencedHierarchyCode")
            }

            Pair(domainRow, hierarchyRow)
        }

        MetricTable.insert {
            it[correspondingMemberCol] = metricMemberId
            it[dataTypeCol] = metric.dataType
            it[flowTypeCol] = metric.flowType
            it[balanceTypeCol] = metric.balanceType
            it[referencedDomainCol] = referencedRows?.first?.get(DomainTable.id)
            it[referencedHierarchyCol] = referencedRows?.second?.get(HierarchyTable.id)
            it[hierarchyStartingMemberCol] = null
            it[isStartingMemberIncludedCol] = null
        }
    }
}
