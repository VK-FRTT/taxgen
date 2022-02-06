package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.validation.ValidatableNestedObject
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNullAndNonBlank
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

data class OrdinateCategorisationSignature(
    val identifierKind: IdentifierKind,
    val signaturePrecision: SignaturePrecision,

    val dimensionIdentifier: String,
    val memberIdentifier: String,
    val isDefaultMemberIncluded: String?,

    val hierarchyIdentifier: String?,
    val hierarchyStartingMemberIdentifier: String?,
    val isStartingMemberIncluded: String?,

    val originSignatureLiteral: String
) : ValidatableNestedObject {
    enum class IdentifierKind {
        DATABASE_ID,
        XBRL_CODE
    }

    enum class SignaturePrecision {
        CLOSED_AXIS,
        SEMI_OPEN_AXIS_PARTIAL_RESTRICTION,
        SEMI_OPEN_AXIS_FULL_RESTRICTION
    }

    companion object {
        val VALID_IS_STARTING_MEMBER_INCLUDED_VALUES = listOf("0", "1")
        val VALID_IS_DEFAULT_MEMBER_INCLUDED_VALUES = listOf("", "?")
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        if (signaturePrecision == SignaturePrecision.CLOSED_AXIS ||
            signaturePrecision == SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION ||
            signaturePrecision == SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION
        ) {
            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::dimensionIdentifier
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::memberIdentifier
            )
        }

        if (signaturePrecision == SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION ||
            signaturePrecision == SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION
        ) {
            validatePropFulfillsCondition(
                validationResultBuilder = validationResultBuilder,
                property = this::isDefaultMemberIncluded,
                condition = { VALID_IS_DEFAULT_MEMBER_INCLUDED_VALUES.contains(isDefaultMemberIncluded) },
                reason = { "Unsupported value `$isDefaultMemberIncluded´" }
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyIdentifier
            )
        }

        if (signaturePrecision == SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION) {
            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberIdentifier
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::isStartingMemberIncluded
            )

            validatePropFulfillsCondition(
                validationResultBuilder = validationResultBuilder,
                property = this::isStartingMemberIncluded,
                condition = { VALID_IS_STARTING_MEMBER_INCLUDED_VALUES.contains(isStartingMemberIncluded) },
                reason = { "Unsupported value" }
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
