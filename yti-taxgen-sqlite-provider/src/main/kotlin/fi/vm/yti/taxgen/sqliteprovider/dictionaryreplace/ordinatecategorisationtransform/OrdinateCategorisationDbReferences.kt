package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNonNull
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
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

    fun validate(validationResults: ValidationResults) {

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationDbReferences::dimensionId
        )

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationDbReferences::memberId
        )

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION ||
            signatureStructure == OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationDbReferences::hierarchyId
            )
        }

        if (signatureStructure == OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = OrdinateCategorisationDbReferences::hierarchyStartingMemberId
            )
        }
    }
}
