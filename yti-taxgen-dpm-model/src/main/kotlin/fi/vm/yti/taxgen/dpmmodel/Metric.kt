package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Metric(
    override val uri: String,
    override val concept: Concept,
    val metricCode: String, //TODO validate & test: metric code has matching dataType & flowType tags from the metric self
    val dataType: String, //TODO validate & test: if data type == Enumeration => referencedDomainCode != null
    val flowType: String?,
    val balanceType: String?,
    val referencedDomainCode: String?, //TODO validate & test: if referencedDomainCode != null => Data type must be Enumeration
    val referencedHierarchyCode: String? //TODO validate & test: if referencedHierarchyCode != null => referencedDomainCode != null
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

        super.validate(validationResults)

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
    }

    override fun code(): String = metricCode
}
