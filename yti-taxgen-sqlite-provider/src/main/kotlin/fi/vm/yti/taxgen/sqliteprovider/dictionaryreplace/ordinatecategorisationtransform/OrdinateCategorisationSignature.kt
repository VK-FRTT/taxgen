package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonBlank

data class OrdinateCategorisationSignature(
    val type: Type,

    val dimensionIdentifier: String,
    val memberIdentifier: String,
    val openAxisValueRestrictionSignature: OpenAxisValueRestrictionSignature?
) {
    enum class Type {
        DATABASE_ID_SIGNATURE,
        XBRL_CODE_SIGNATURE
    }

    fun hasOpenAxisValueRestrictionSignature(): Boolean {
        return openAxisValueRestrictionSignature != null
    }

    fun validate(validationResults: ValidationResults) {
        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationSignature::dimensionIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationSignature::memberIdentifier
        )

        openAxisValueRestrictionSignature?.validate(validationResults)
    }
}
