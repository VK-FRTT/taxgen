package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNullAndNonBlank
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.id.EntityID
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

    fun validate(validationResultBuilder: ValidationResultBuilder) {
        validateNonNullAndNonBlank(
            validationResultBuilder = validationResultBuilder,
            property = this::dimensionIdentifier
        )

        validateNonNullAndNonBlank(
            validationResultBuilder = validationResultBuilder,
            property = this::memberIdentifier
        )

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyIdentifier
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberIdentifier
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::startingMemberIncluded
            )

            validatePropFulfillsCondition(
                validationResultBuilder = validationResultBuilder,
                property = this::startingMemberIncluded,
                condition = { VALID_STARTING_MEMBER_INCLUDED_VALUES.contains(startingMemberIncluded) },
                reason = { "Unsupported value" }
            )
        }

        if (signatureStructure == OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyIdentifier
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
