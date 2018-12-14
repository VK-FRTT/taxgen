package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateDpmElementRef
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class TypedDimension(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val dimensionCode: String,
    val domainRef: DpmElementRef
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
