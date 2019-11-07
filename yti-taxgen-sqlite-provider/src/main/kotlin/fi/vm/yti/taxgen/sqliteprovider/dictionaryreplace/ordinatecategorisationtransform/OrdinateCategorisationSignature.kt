package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNonNullAndNonBlank
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

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
        validateNonNullAndNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationSignature::dimensionIdentifier
        )

        validateNonNullAndNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationSignature::memberIdentifier
        )

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNullAndNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyIdentifier
            )

            validateNonNullAndNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyStartingMemberIdentifier
            )

            validateNonNullAndNonBlank(
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
            validateNonNullAndNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationSignature::hierarchyIdentifier
            )
        }
    }

    fun lookupHierarchyCodeForHierarchyIdentifier(): String? {
        require(identifierKind == IdentifierKind.DATABASE_ID)

        return transaction {
            hierarchyIdentifier?.toIntOrNull()?.let {
                HierarchyTable.rowWhereHierarchyId(
                    EntityID(
                        it,
                        HierarchyTable
                    )
                )?.get(HierarchyTable.hierarchyCodeCol)
            }
        }
    }

    fun lookupMemberCodeForHierarchyStartingMemberIdentifier(): String? {
        require(identifierKind == IdentifierKind.DATABASE_ID)

        return transaction {
            hierarchyStartingMemberIdentifier?.toIntOrNull()?.let {
                MemberTable.rowWhereMemberId(
                    EntityID(
                        it,
                        MemberTable
                    )
                )?.get(MemberTable.memberCodeCol)
            }
        }
    }
}
