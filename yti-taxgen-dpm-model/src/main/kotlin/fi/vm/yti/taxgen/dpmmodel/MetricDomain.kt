package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmElementCrossReferences
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropsLengths

data class MetricDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val metrics: List<Metric>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder, 0)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validatePropsLengths(
            validationResultBuilder = validationResultBuilder,
            properties = listOf(this::metrics, this::hierarchies),
            minLength = 0,
            maxLength = 10000
        )

        // Note: metrics & hierarchies URI and code uniqueness is validated DPM Model level

        hierarchies.forEach { hierarchy ->
            validationResultBuilder.withSubject(hierarchy.validationSubjectDescriptor()) {
                validateDpmElementCrossReferences(
                    validationResultBuilder = validationResultBuilder,
                    targetElements = metrics,
                    targetCodeProperty = Metric::metricCode,
                    referringElements = hierarchy.allNodes(),
                    referringCodeProperty = HierarchyNode::referencedElementCode
                )
            }
        }
    }

    override fun code(): String = domainCode
}
