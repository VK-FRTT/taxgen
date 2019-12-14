package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.sqliteoutput.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

data class OrdinateCategorisationDbReferences(

    val signatureStructure: OrdinateCategorisationSignatureStructure,

    val dimensionId: EntityID<Int>?,
    val memberId: EntityID<Int>?,

    val hierarchyId: EntityID<Int>?,
    val hierarchyStartingMemberId: EntityID<Int>?
) {
    companion object {
        private const val OPEN_MEMBER_MARKER = "*"

        fun fromOrdinateCategorisationXbrlCodeSignature(
            signature: OrdinateCategorisationSignature
        ): OrdinateCategorisationDbReferences {

            require(signature.identifierKind == OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE)

            return transaction {
                val dimensionRow = DimensionTable.rowWhereXbrlCode(signature.dimensionIdentifier)

                val memberRow = if (signature.memberIdentifier == OPEN_MEMBER_MARKER) {
                    MemberTable.openMemberRow()
                } else {
                    MemberTable.rowWhereMemberXbrlCode(signature.memberIdentifier)
                }

                val hierarchyRow = signature.hierarchyIdentifier?.run {
                    val scopingDomainId = dimensionRow?.get(DimensionTable.domainIdCol)

                    scopingDomainId?.run {
                        HierarchyTable.rowWhereDomainIdAndHierarchyCode(
                            scopingDomainId,
                            signature.hierarchyIdentifier
                        )
                    }
                }

                val hierarchyNodeRow = signature.hierarchyStartingMemberIdentifier?.run {
                    val hierarchyId = hierarchyRow?.get(HierarchyTable.id)

                    hierarchyId?.run {
                        HierarchyNodeTable.rowWhereHierarchyIdAndMemberCode(
                            hierarchyId,
                            signature.hierarchyStartingMemberIdentifier
                        )
                    }
                }

                OrdinateCategorisationDbReferences(
                    signatureStructure = signature.signatureStructure,
                    dimensionId = dimensionRow?.get(DimensionTable.id),
                    memberId = memberRow?.get(MemberTable.id),
                    hierarchyId = hierarchyRow?.get(HierarchyTable.id),
                    hierarchyStartingMemberId = hierarchyNodeRow?.get(HierarchyNodeTable.memberIdCol)
                )
            }
        }
    }

    fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateNonNull(
            validationResultBuilder = validationResultBuilder,
            property = this::dimensionId
        )

        validateNonNull(
            validationResultBuilder = validationResultBuilder,
            property = this::memberId
        )

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION ||
            signatureStructure == OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyId
            )
        }

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberId
            )
        }
    }
}
