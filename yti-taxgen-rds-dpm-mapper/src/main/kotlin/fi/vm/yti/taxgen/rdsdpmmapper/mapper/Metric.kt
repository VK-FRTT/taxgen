package fi.vm.yti.taxgen.rdsdpmmapper.mapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader

internal fun mapAndValidateMetrics(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<Metric> {
    val metrics = mutableListOf<Metric>()

    if (codeListSource == null) return metrics

    //Base details
    codeListSource.eachCode { code ->
        val metric = Metric(
            id = code.idOrEmpty(),
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            memberCodeNumber = code.codeValueOrEmpty(),
            dataType = "",
            flowType = "",
            balanceType = "",
            referencedDomainCode = null,
            referencedHierarchyCode = null
        )

        metrics.add(metric)
    }

    //Extension based details
    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmMetric)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val metric = metrics.find { it.uri == codeUri }

                if (metric != null) {

                    val updatedMetric = metric.copy(
                        dataType = extensionMember.stringValueOrEmpty(RdsMemberValueType.DpmMetricDataType),
                        flowType = extensionMember.stringValueOrEmpty(RdsMemberValueType.DpmFlowType),
                        balanceType = extensionMember.stringValueOrEmpty(RdsMemberValueType.DpmBalanceType),
                        referencedDomainCode = extensionMember.stringValueOrNull(RdsMemberValueType.DpmDomainReference),
                        referencedHierarchyCode = extensionMember.stringValueOrNull(RdsMemberValueType.DpmHierarchyReference)
                    )

                    metrics.replaceOrAddByUri(updatedMetric)
                }
            }
        }
    }

    diagnostic.validate(metrics)

    return metrics
}
