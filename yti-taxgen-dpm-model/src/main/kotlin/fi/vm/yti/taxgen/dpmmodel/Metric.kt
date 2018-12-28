package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateOptionalDpmElementRef

data class Metric(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val memberCodeNumber: Int,
    val dataType: Metric.DataType,
    val flowType: Metric.FlowType,
    val balanceType: Metric.BalanceType,
    val domainRef: DpmElementRef?,  //TODO - validate refs points to *Domain?
    val hierarchyRef: DpmElementRef? //TODO - validate refs points to Hierarchy?
) : DpmElement {

    enum class DataType {
        ENUMERATION,
        BOOLEAN,
        DATE,
        INTEGER,
        MONETARY,
        PERCENTAGE,
        STRING,
        DECIMAL,
        LEI, //TODO - Should LEI & ISIN be removed from the set?
        ISIN
    }

    enum class FlowType {
        INSTANT,
        DURATION
    }

    enum class BalanceType {
        CREDIT,
        DEBIT
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
