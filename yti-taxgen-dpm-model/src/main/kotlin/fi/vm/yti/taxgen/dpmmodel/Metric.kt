package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateOptionalDpmElementRef

data class Metric(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val memberCodeNumber: Int,
    val dataType: String,
    val flowType: String,
    val balanceType: String,
    val domainRef: DpmElementRef?,  //TODO - validate refs points to *Domain?
    val hierarchyRef: DpmElementRef? //TODO - validate refs points to Hierarchy?
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
            condition = { memberCodeNumber >= 0 },
            message = { "negative member code number" }
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

        validateOptionalDpmElementRef(
            validationResults = validationResults,
            instance = this,
            property = Metric::domainRef
        )

        validateOptionalDpmElementRef(
            validationResults = validationResults,
            instance = this,
            property = Metric::hierarchyRef
        )
    }
}
