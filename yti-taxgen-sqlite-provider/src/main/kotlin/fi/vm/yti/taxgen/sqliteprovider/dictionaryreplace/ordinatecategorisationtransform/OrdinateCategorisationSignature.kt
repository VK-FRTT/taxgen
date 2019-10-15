package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonBlank

data class OrdinateCategorisationSignature(
    val identifierKind: IdentifierKind,
    val signatureStructure: OrdinateCategorisationSignatureStructure,

    val dimensionIdentifier: String,
    val memberIdentifier: String,
    val hierarchyIdentifier: String?,
    val hierarchyStartingMemberIdentifier: String?,
    val startingMemberIncluded: String?
) {
    enum class IdentifierKind {
        DATABASE_ID,
        XBRL_CODE
    }

    companion object {
        val VALID_STARTING_MEMBER_INCLUDED_VALUES = listOf("0", "1")
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

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyIdentifier
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyStartingMemberIdentifier
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::startingMemberIncluded
            )

            validateConditionTruthy(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::startingMemberIncluded,
                condition = { VALID_STARTING_MEMBER_INCLUDED_VALUES.contains(startingMemberIncluded) },
                message = { "unsupported IsStartingMemberIncluded value '$startingMemberIncluded'" }
            )
        }

        if (signatureStructure == OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyIdentifier
            )
        }
    }
}
