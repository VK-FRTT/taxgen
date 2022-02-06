package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.validation.ValidatableNestedObject
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.sqliteoutput.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

data class OrdinateCategorisationDbReferences(

    val signature: OrdinateCategorisationSignature,

    val dimensionId: EntityID<Int>?,
    val memberId: EntityID<Int>?,
    val hierarchyId: EntityID<Int>?,
    val hierarchyStartingMemberId: EntityID<Int>?,

    val dimensionIdReasonDetail: String,
    val memberIdReasonDetail: String,
    val hierarchyIdReasonDetail: String,
    val hierarchyStartingMemberIdReasonDetail: String
) : ValidatableNestedObject {
    companion object {
        private const val OPEN_MEMBER_MARKER = "*"

        fun fromOrdinateCategorisationXbrlCodeSignature(
            signature: OrdinateCategorisationSignature
        ): OrdinateCategorisationDbReferences {

            require(signature.identifierKind == OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE)

            return transaction {
                val dimensionRow = DimensionTable.rowWhereXbrlCode(signature.dimensionIdentifier)
                val dimensionIdReasonDetail = "No Dimension with XBRL code `${signature.dimensionIdentifier}´"

                val memberRow = if (signature.memberIdentifier == OPEN_MEMBER_MARKER) {
                    MemberTable.openMemberRow()
                } else {
                    MemberTable.rowWhereMemberXbrlCode(signature.memberIdentifier)
                }
                val memberIdReasonDetail = "No Member with XBRL code `${signature.memberIdentifier}´"

                val scopingDomainId = dimensionRow?.get(DimensionTable.domainIdCol)
                val hierarchyRow = signature.hierarchyIdentifier?.run {
                    scopingDomainId?.run {
                        HierarchyTable.rowWhereDomainIdAndHierarchyCode(
                            scopingDomainId,
                            signature.hierarchyIdentifier
                        )
                    }
                }
                val hierarchyIdReasonDetail = "No Hierarchy with HierarchyCode `${signature.hierarchyIdentifier}´ within Domain `$scopingDomainId´"

                val scopingHierarchyId = hierarchyRow?.get(HierarchyTable.id)
                val hierarchyNodeRow = signature.hierarchyStartingMemberIdentifier?.run {

                    scopingHierarchyId?.run {
                        HierarchyNodeTable.rowWhereHierarchyIdAndMemberCode(
                            scopingHierarchyId,
                            signature.hierarchyStartingMemberIdentifier
                        )
                    }
                }
                val hierarchyStartingMemberIdReasonDetail = "No HierarchyNode with MemberCode `${signature.hierarchyStartingMemberIdentifier}´ within Hierarchy `$scopingHierarchyId´"

                OrdinateCategorisationDbReferences(
                    signature = signature,
                    dimensionId = dimensionRow?.get(DimensionTable.id),
                    memberId = memberRow?.get(MemberTable.id),
                    hierarchyId = hierarchyRow?.get(HierarchyTable.id),
                    hierarchyStartingMemberId = hierarchyNodeRow?.get(HierarchyNodeTable.memberIdCol),

                    dimensionIdReasonDetail = dimensionIdReasonDetail,
                    memberIdReasonDetail = memberIdReasonDetail,
                    hierarchyIdReasonDetail = hierarchyIdReasonDetail,
                    hierarchyStartingMemberIdReasonDetail = hierarchyStartingMemberIdReasonDetail
                )
            }
        }
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        if (signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.CLOSED_AXIS ||
            signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION ||
            signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::dimensionId,
                reasonDetail = dimensionIdReasonDetail
            )

            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::memberId,
                reasonDetail = memberIdReasonDetail
            )
        }

        if (signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION ||
            signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyId,
                reasonDetail = hierarchyIdReasonDetail
            )
        }

        if (signature.signaturePrecision == OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberId,
                reasonDetail = hierarchyStartingMemberIdReasonDetail
            )
        }
    }
}
