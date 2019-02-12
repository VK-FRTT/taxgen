package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DomainLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.FixedEntitiesLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.MemberLookupItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbMetric {

    fun writeMetricDomainMembers(
        metricDomain: MetricDomain,
        owner: Owner,
        languageIds: Map<Language, EntityID<Int>>,
        ownerId: EntityID<Int>,
        domainLookupItems: List<DomainLookupItem>,
        fixedEntitiesLookupItem: FixedEntitiesLookupItem
    ): Pair<EntityID<Int>, List<MemberLookupItem>> {

        return transaction {

            val domainId = fixedEntitiesLookupItem.metricDomainId

            val memberLookupItems = metricDomain.metrics.map { metric ->

                val metricMemberConceptId = DbConcepts.writeConceptAndTranslations(
                    metric.concept,
                    ConceptType.MEMBER,
                    ownerId,
                    languageIds
                )

                val (metricMemberId, memberXbrlCode) = insertMetricMember(
                    metric,
                    owner,
                    metricMemberConceptId,
                    domainId
                )

                insertMetric(
                    metric,
                    metricMemberId,
                    domainLookupItems
                )

                MemberLookupItem(
                    memberUri = metric.uri,
                    memberXbrlCode = memberXbrlCode,
                    defaultLabelText = metric.concept.label.defaultTranslation(),
                    memberId = metricMemberId
                )
            }

            Pair(domainId, memberLookupItems)
        }
    }

    private fun insertMetricMember(
        metric: Metric,
        owner: Owner,
        metricMemberConceptId: EntityID<Int>,
        metricDomainId: EntityID<Int>
    ): Pair<EntityID<Int>, String> {
        val memberXbrlCode = "${owner.prefix}_met:${metric.metricCode}"

        val memberId = MemberTable.insertAndGetId {
            it[memberCodeCol] = metric.metricCode
            it[memberLabelCol] = metric.concept.label.defaultTranslation()
            it[memberXBRLCodeCol] = memberXbrlCode
            it[isDefaultMemberCol] = false
            it[conceptIdCol] = metricMemberConceptId
            it[domainIdCol] = metricDomainId
        }

        return Pair(memberId, memberXbrlCode)
    }

    private fun insertMetric(
        metric: Metric,
        metricMemberId: EntityID<Int>,
        domainLookupItems: List<DomainLookupItem>
    ) {
        val referencedDomainItem =
            domainLookupItems.find { it.domainCode == metric.referencedDomainCode }

        val referencedHierarchyItem =
            referencedDomainItem?.hierarchyLookupItems?.find { it.hierarchyCode == metric.referencedHierarchyCode }

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
