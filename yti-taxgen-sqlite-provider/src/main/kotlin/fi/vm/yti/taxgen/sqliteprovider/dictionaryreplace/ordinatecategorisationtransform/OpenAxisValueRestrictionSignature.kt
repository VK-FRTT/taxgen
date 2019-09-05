package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonBlank

class OpenAxisValueRestrictionSignature(
    val hierarchyIdentifier: String,
    val hierarchyStartingMemberIdentifier: String,
    val startingMemberIncluded: String
) {
    companion object {
        val VALID_STARTING_MEMBER_INCLUDED_VALUES = listOf("0", "1")
    }

    fun validate(validationResults: ValidationResults) {
        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionSignature::hierarchyIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionSignature::hierarchyStartingMemberIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionSignature::startingMemberIncluded
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionSignature::startingMemberIncluded,
            condition = { VALID_STARTING_MEMBER_INCLUDED_VALUES.contains(startingMemberIncluded) },
            message = { "unsupported IsStartingMemberIncluded value '$startingMemberIncluded'" }
        )
    }
}
