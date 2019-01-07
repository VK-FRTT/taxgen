package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmElementRef
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class TypedDimension(
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val domainRef: DpmElementRef //TODO - validate ref points to TypedDomain?
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = TypedDimension::dimensionCode,
            minLength = 2,
            maxLength = 50
        )

        validateDpmElementRef(
            validationResults = validationResults,
            instance = this,
            property = TypedDimension::domainRef
        )
    }
}
