package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

data class OrdinateCategorisationDbReferences(

    val dimensionId: EntityID<Int>?,
    val memberId: EntityID<Int>?,
    val openAxisValueRestrictionDbReferences: OpenAxisValueRestrictionDbReferences?
) {
    companion object {
        private const val OPEN_MEMBER_MARKER = "*"

        fun fromOrdinateCategorisationXbrlCodeSignature(
            signature: OrdinateCategorisationSignature
        ): OrdinateCategorisationDbReferences {

            require(signature.type == OrdinateCategorisationSignature.Type.XBRL_CODE_SIGNATURE)

            return transaction {
                val dimensionRow = DimensionTable.rowWhereXbrlCode(signature.dimensionIdentifier)
                val dimensionId = dimensionRow?.get(DimensionTable.id)

                val memberRow = if (signature.memberIdentifier == OPEN_MEMBER_MARKER) {
                    MemberTable.openMemberRow()
                } else {
                    MemberTable.rowWhereMemberXbrlCode(signature.memberIdentifier)
                }
                val memberId = memberRow?.get(MemberTable.id)

                val openAxisValueRestrictionRelationships =
                    signature.openAxisValueRestrictionSignature?.run {

                        val scopingDomainId = dimensionRow?.get(DimensionTable.domainIdCol)

                        val hierarchyRow = scopingDomainId?.run {
                            HierarchyTable.rowWhereDomainIdAndHierarchyCode(
                                scopingDomainId,
                                signature.openAxisValueRestrictionSignature.hierarchyIdentifier
                            )
                        }

                        val hierarchyId = hierarchyRow?.get(HierarchyTable.id)

                        val hierarchyNodeRow = hierarchyId?.run {
                            HierarchyNodeTable.rowWhereHierarchyIdAndMemberCode(
                                hierarchyId,
                                signature.openAxisValueRestrictionSignature.hierarchyStartingMemberIdentifier
                            )
                        }

                        val hierarchyStartingMemberId = hierarchyNodeRow?.get(HierarchyNodeTable.memberIdCol)

                        OpenAxisValueRestrictionDbReferences(
                            hierarchyId = hierarchyId,
                            hierarchyStartingMemberId = hierarchyStartingMemberId
                        )
                    }

                OrdinateCategorisationDbReferences(
                    dimensionId = dimensionId,
                    memberId = memberId,
                    openAxisValueRestrictionDbReferences = openAxisValueRestrictionRelationships
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

        openAxisValueRestrictionDbReferences?.validate(validationResults)
    }
}
