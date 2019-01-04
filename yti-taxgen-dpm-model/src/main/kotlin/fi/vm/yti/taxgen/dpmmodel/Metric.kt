package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy

data class Metric(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val memberCodeNumber: String,
    val dataType: String,
    val flowType: String,
    val balanceType: String,
    val referencedDomainCode: String?, //TODO - validation? Ref points to *Domain?
    val referencedHierarchyCode: String? //TODO - validation? Ref points to Hierarchy?
) : DpmElement {

    companion object {
        val VALID_DATA_TYPES = listOf(
            "Enumeration",
            "Boolean",
            "Date",
            "Integer",
            "Monetary",
            "Percentage",
            "String",
            "Decimal",
            "Lei",
            "Isin"
        )

        val VALID_FLOW_TYPES = listOf(
            "Instant",
            "Duration"
        )

        val VALID_BALANCE_TYPES = listOf(
            "Credit",
            "Debit"
        )
    }

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::memberCodeNumber,
            condition = { memberCodeNumber.all { char -> char.isDigit() } },
            message = { "contains non-digit characters" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::dataType,
            condition = { VALID_DATA_TYPES.contains(dataType) },
            message = { "unsupported data type '$dataType'" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = Metric::flowType,
            condition = { VALID_FLOW_TYPES.contains(flowType) },
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
}
