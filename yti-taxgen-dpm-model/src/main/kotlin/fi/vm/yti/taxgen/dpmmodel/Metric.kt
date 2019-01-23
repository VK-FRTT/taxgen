package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmCodeContent
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Metric(
    override val uri: String,
    override val concept: Concept,
    val metricCode: String, //TODO - validate + test metric code follows required syntax (dataType & flowType tags + valid numeric part)
    val dataType: String,
    val flowType: String?,
    val balanceType: String?,
    val referencedDomainCode: String?,
    val referencedHierarchyCode: String?
) : DpmElement {

    companion object {
        val VALID_DATA_TYPES = mapOf(
            "Enumeration/Code" to "e",
            "Boolean" to "b",
            "Date" to "d",
            "Integer" to "i",
            "Monetary" to "m",
            "Percent" to "p",
            "String" to "s",
            "Decimal" to "d",
            "Lei" to "l",
            "Isin" to "i"
        )

        val VALID_FLOW_TYPES = mapOf(
            "Stock" to "i",
            "Flow" to "d",
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
    }

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Metric::metricCode,
            minLength = 1,
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
    }

    override fun code(): String = metricCode
}
