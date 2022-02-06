package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropNullOrNonBlank

data class Metric(
    override val uri: String,
    override val concept: Concept,
    val metricCode: String,
    val dataType: String,
    val flowType: String?,
    val balanceType: String?,
    val referencedDomainCode: String?,
    val referencedHierarchyCode: String?
) : DpmElement {

    companion object {
        val VALID_DATA_TYPES = mapOf(
            "Boolean" to "b",
            "Date" to "d",
            "Decimal" to "d",
            "Enumeration/Code" to "e",
            "Integer" to "i",
            "Isin" to "i",
            "Lei" to "l",
            "Monetary" to "m",
            "Percent" to "p",
            "String" to "s"
        )

        val VALID_FLOW_TYPES = mapOf(
            "Flow" to "d",
            "Stock" to "i",
            null to null
        )

        val VALID_BALANCE_TYPES = listOf(
            "Credit",
            "Debit",
            null
        )

        fun codeTagFromDataType(dataType: String): String {
            return VALID_DATA_TYPES[dataType] ?: "?"
        }

        fun codeTagFromFlowType(flowType: String?): String {
            if (flowType == null) return ""

            return VALID_FLOW_TYPES[flowType] ?: "?"
        }

        private val DATA_TYPE_MARKERS = VALID_DATA_TYPES.values.toSet().joinToString(separator = "")
        private val FLOW_TYPE_MARKERS = VALID_FLOW_TYPES.values.filterNotNull().toSet().joinToString(separator = "")
        private val METRIC_CODE_PATTERN = "\\A[$DATA_TYPE_MARKERS][$FLOW_TYPE_MARKERS]?[0-9]+\\z".toRegex()

        fun isValidMetricCode(metricCode: String): Boolean {
            if (METRIC_CODE_PATTERN.matches(metricCode)) {
                return true
            }

            return false
        }
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::metricCode,
            minLength = 2,
            maxLength = 50
        )

        validateDpmCodeContent(
            validationResultBuilder = validationResultBuilder,
            property = ::metricCode
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::dataType,
            condition = { VALID_DATA_TYPES.containsKey(it) },
            reason = { "Unsupported value" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedDomainCode,
            condition = { (dataType != "Enumeration/Code") || (referencedDomainCode != null) },
            reason = { "Value missing but DataType is Enumeration/Code" },
            includeValueToError = false
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::flowType,
            condition = { VALID_FLOW_TYPES.containsKey(it) },
            reason = { "Unsupported value" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::balanceType,
            condition = { VALID_BALANCE_TYPES.contains(it) },
            reason = { "Unsupported value" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::metricCode,
            condition = { isValidMetricCode(it) },
            reason = { "Unsupported code structure" }
        )

        validatePropNullOrNonBlank(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedDomainCode
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedDomainCode,
            condition = { (referencedDomainCode == null) || (dataType == "Enumeration/Code") },
            reason = { "Value given but DataType not Enumeration/Code" }
        )

        validatePropNullOrNonBlank(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedHierarchyCode
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedHierarchyCode,
            condition = { (referencedHierarchyCode == null) || (dataType == "Enumeration/Code") },
            reason = { "Value given but DataType not Enumeration/Code" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedDomainCode,
            condition = { (referencedHierarchyCode == null) || (referencedDomainCode != null) },
            reason = { "Value missing but ReferencedHierarchyCode is given" },
            includeValueToError = false
        )
    }

    override fun code(): String = metricCode
}
