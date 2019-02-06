package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.sqliteprovider.conceptitem.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitem.MemberItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object DbMetric {

    fun writeMetricDomainMembers(
        dictionaryItem: DpmDictionaryItem,
        metricDomain: MetricDomain
    ): Pair<EntityID<Int>, Map<String, MemberItem>> {

        return transaction {

            val metricDomainId = lookupMetricDomainId()

            val metricMemberIds = metricDomain.metrics.map { metric ->

                val metricMemberConceptId = DbConcepts.writeConceptAndTranslations(
                    dictionaryItem,
                    metric.concept,
                    ConceptType.MEMBER
                )

                val metricMemberId = insertMetricMember(
                    dictionaryItem,
                    metricDomainId,
                    metric,
                    metricMemberConceptId
                )

                insertMetric(
                    dictionaryItem,
                    metric,
                    metricMemberId
                )

                metric.uri to MemberItem(
                    memberId = metricMemberId,
                    defaultLabelText = metric.concept.label.defaultTranslation()
                )
            }.toMap()

            Pair(metricDomainId, metricMemberIds)
        }
    }

    private fun lookupMetricDomainId(): EntityID<Int> {
        val metDomain = DomainTable.select { DomainTable.domainCodeCol eq "MET" }.firstOrNull()
            ?: thisShouldNeverHappen("Missing MET domain")

        return metDomain[DomainTable.id]
    }

    private fun insertMetricMember(
        dictionaryItem: DpmDictionaryItem,
        metricDomainId: EntityID<Int>,
        metric: Metric,
        metricMemberConceptId: EntityID<Int>
    ): EntityID<Int> {
        val memberXbrlCode = "${dictionaryItem.owner.prefix}_met:${metric.metricCode}"

        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = metric.metricCode
            it[memberLabelCol] = metric.concept.label.defaultTranslation()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = false
            it[conceptIdCol] = metricMemberConceptId
            it[domainIdCol] = metricDomainId
        }

        return memberId
    }

    private fun insertMetric(
        dictionaryItem: DpmDictionaryItem,
        metric: Metric,
        metricMemberId: EntityID<Int>
    ) {
        val referencedDomainItem = dictionaryItem.optionalDomainItemForCode(metric.referencedDomainCode)
        val referencedHierarchyItem = referencedDomainItem?.optionalHierarchyItemForCode(metric.referencedHierarchyCode)

        MetricTable.insert {
            it[correspondingMemberCol] = metricMemberId
            it[dataTypeCol] = metric.dataType
            it[flowTypeCol] = metric.flowType
            it[balanceTypeCol] = metric.balanceType
            it[referencedDomainCol] = referencedDomainItem?.domainId
            it[referencedHierarchyCol] = referencedHierarchyItem?.hierarchyId
            it[hierarchyStartingMemberCol] = null
            it[isStartingMemberIncludedCol] = null
        }
    }
}
