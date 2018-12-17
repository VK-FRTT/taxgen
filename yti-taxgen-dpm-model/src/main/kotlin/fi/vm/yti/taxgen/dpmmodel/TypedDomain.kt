package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class TypedDomain(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val dataType: TypedDomain.DataType
) : DpmElement {

    enum class DataType {
        BOOLEAN,
        DATE,
        INTEGER,
        MONETARY,
        PERCENTAGE,
        STRING,
        DECIMAL,
        LEI,
        ISIN
    }

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = TypedDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )
    }
}
