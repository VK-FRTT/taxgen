package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateLength
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNullOrNonBlank

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

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Metric::metricCode,
            minLength = 2,
            maxLength = 50
        )

        validateDpmCodeContent(
            validationResults = validationResults,
            instance = this,
            property = Metric::metricCode
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::dataType,
            condition = { VALID_DATA_TYPES.containsKey(dataType) },
            message = { "unsupported data type '$dataType'" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::dataType,
            condition = { (dataType != "Enumeration/Code") || (referencedDomainCode != null) },
            message = { "missing ReferencedDomainCode when data type is Enumeration/Code" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::flowType,
            condition = { VALID_FLOW_TYPES.containsKey(flowType) },
            message = { "unsupported flow type '$flowType'" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::balanceType,
            condition = { VALID_BALANCE_TYPES.contains(balanceType) },
            message = { "unsupported balance type '$balanceType'" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::metricCode,
            condition = { isValidMetricCode(metricCode) },
            message = { "metric code does not match required pattern '$metricCode'" }
        )

        validateNullOrNonBlank(
            validationResults = validationResults,
            instance = this,
            property = Metric::referencedDomainCode
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::dataType,
            condition = { (referencedDomainCode == null) || (dataType == "Enumeration/Code") },
            message = { "ReferencedDomainCode given but data type not Enumeration/Code" }
        )

        validateNullOrNonBlank(
            validationResults = validationResults,
            instance = this,
            property = Metric::referencedHierarchyCode
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::dataType,
            condition = { (referencedHierarchyCode == null) || (dataType == "Enumeration/Code") },
            message = { "ReferencedHierarchyCode given but data type not Enumeration/Code" }
        )
    }

    override fun code(): String = metricCode
}
