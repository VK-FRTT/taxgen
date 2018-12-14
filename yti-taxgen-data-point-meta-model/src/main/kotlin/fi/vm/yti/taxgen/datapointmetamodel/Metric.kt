package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateOptionalDpmElementRef

data class Metric(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val memberCodeNumber: Int,
    val dataType: Metric.DataType,
    val flowType: Metric.FlowType,
    val balanceType: Metric.BalanceType,
    val domainRef: DpmElementRef?,
    val hierarchyRef: DpmElementRef?
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
