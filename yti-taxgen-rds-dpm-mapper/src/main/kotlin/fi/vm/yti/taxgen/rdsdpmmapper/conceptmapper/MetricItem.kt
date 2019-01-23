package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.Metric

data class MetricItem(
    override val uri: String,
    override val concept: Concept,
    val metricCodeValue: String,
    val dataType: String,
    val flowType: String?,
    val balanceType: String?,
    val referencedDomainCode: String?,
    val referencedHierarchyCode: String?
) : DpmElement {

    fun toMetric(): Metric {

        val metricCode =
            Metric.codeTagFromDataType(dataType) +
                Metric.codeTagFromFlowType(flowType) +
                metricCodeValue

        return Metric(
            uri = uri,
            concept = concept,
            metricCode = metricCode,
            dataType = dataType,
            flowType = flowType,
            balanceType = balanceType,
            referencedDomainCode = referencedDomainCode,
            referencedHierarchyCode = referencedHierarchyCode
        )
    }
}
