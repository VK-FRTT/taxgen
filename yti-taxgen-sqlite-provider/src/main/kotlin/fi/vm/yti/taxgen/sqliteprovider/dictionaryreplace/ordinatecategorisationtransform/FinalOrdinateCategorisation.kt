package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

data class FinalOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    val relationships: FinalOrdinateCategorisation.Relationships,

    val databaseIdSignature: String,
    val xbrlCodeSignature: String,

    val source: String?
) : Validatable {

    data class Relationships(
        val dimensionId: EntityID<Int>?,
        val memberId: EntityID<Int>?,
        val openAxisValueRestrictionRelationships: OpenAxisValueRestrictionRelationships?
    ) {
        fun validate(validationResults: ValidationResults) {

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = Relationships::dimensionId
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = Relationships::memberId
            )

            openAxisValueRestrictionRelationships?.validate(validationResults)
        }
    }

    data class OpenAxisValueRestrictionRelationships(
        val hierarchyId: EntityID<Int>?,
        val hierarchyStartingMemberId: EntityID<Int>?
    ) {
        fun validate(validationResults: ValidationResults) {

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionRelationships::hierarchyId
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionRelationships::hierarchyStartingMemberId
            )
        }
    }

    companion object {

        private const val OPEN_MEMBER_MARKER = "*"

        fun fromBaseline(
            baseline: BaselineOrdinateCategorisation
        ): FinalOrdinateCategorisation {
            val relationships = resolveRelationships(baseline)

            return FinalOrdinateCategorisation(
                ordinateId = baseline.ordinateId,
                source = baseline.source,
                relationships = relationships,
                databaseIdSignature = composeDatabaseIdSignature(baseline, relationships),
                xbrlCodeSignature = composeXbrlCodeSignature(baseline)
            )
        }

        private fun resolveRelationships(
            baseline: BaselineOrdinateCategorisation
        ): FinalOrdinateCategorisation.Relationships {
            val xbrlCodeSignature = baseline.xbrlCodeSignature

            return transaction {
                val dimensionRow = DimensionTable.rowWhereXbrlCode(xbrlCodeSignature.dimensionIdentifier)
                val dimensionId = dimensionRow?.get(DimensionTable.id)

                val memberRow = if (xbrlCodeSignature.memberIdentifier == OPEN_MEMBER_MARKER) {
                    MemberTable.openMemberRow()
                } else {
                    MemberTable.rowWhereXbrlCode(xbrlCodeSignature.memberIdentifier)
                }
                val memberId = memberRow?.get(MemberTable.id)

                val openAxisValueRestrictionRelationships =
                    xbrlCodeSignature.openAxisValueRestrictionSignature?.run {

                        val scopingDomainId = dimensionRow?.get(DimensionTable.domainIdCol)

                        val hierarchyRow = scopingDomainId?.run {
                            HierarchyTable.rowWhereDomainIdAndHierarchyCode(
                                scopingDomainId,
                                xbrlCodeSignature.openAxisValueRestrictionSignature.hierarchyIdentifier
                            )
                        }

                        val hierarchyId = hierarchyRow?.get(HierarchyTable.id)

                        val hierarchyNodeRow = hierarchyId?.run {
                            HierarchyNodeTable.rowWhereHierarchyIdAndMemberCode(
                                hierarchyId,
                                xbrlCodeSignature.openAxisValueRestrictionSignature.hierarchyStartingMemberIdentifier
                            )
                        }

                        val hierarchyStartingMemberId = hierarchyNodeRow?.get(HierarchyNodeTable.memberIdCol)

                        OpenAxisValueRestrictionRelationships(
                            hierarchyId = hierarchyId,
                            hierarchyStartingMemberId = hierarchyStartingMemberId
                        )
                    }

                FinalOrdinateCategorisation.Relationships(
                    dimensionId = dimensionId,
                    memberId = memberId,
                    openAxisValueRestrictionRelationships = openAxisValueRestrictionRelationships
                )
            }
        }

        private fun composeDatabaseIdSignature(
            baseline: BaselineOrdinateCategorisation,
            relationships: FinalOrdinateCategorisation.Relationships
        ): String {
            return doComposeSignature(
                dimension = baseline.xbrlCodeSignature.dimensionIdentifier,
                member = baseline.xbrlCodeSignature.memberIdentifier,
                openAxisValueRestrictionPresent = (relationships.openAxisValueRestrictionRelationships != null),
                hierarchy = relationships.openAxisValueRestrictionRelationships?.hierarchyId,
                startingMember = relationships.openAxisValueRestrictionRelationships?.hierarchyStartingMemberId,
                startingMemberIncluded = baseline.xbrlCodeSignature.openAxisValueRestrictionSignature?.startingMemberIncluded
            )
        }

        private fun composeXbrlCodeSignature(
            baseline: BaselineOrdinateCategorisation
        ): String {
            return doComposeSignature(
                dimension = baseline.xbrlCodeSignature.dimensionIdentifier,
                member = baseline.xbrlCodeSignature.memberIdentifier,
                openAxisValueRestrictionPresent = (baseline.xbrlCodeSignature.openAxisValueRestrictionSignature != null),
                hierarchy = baseline.xbrlCodeSignature.openAxisValueRestrictionSignature?.hierarchyIdentifier,
                startingMember = baseline.xbrlCodeSignature.openAxisValueRestrictionSignature?.hierarchyStartingMemberIdentifier,
                startingMemberIncluded = baseline.xbrlCodeSignature.openAxisValueRestrictionSignature?.startingMemberIncluded
            )
        }

        private fun doComposeSignature(
            dimension: String,
            member: String,
            openAxisValueRestrictionPresent: Boolean,
            hierarchy: Any?,
            startingMember: Any?,
            startingMemberIncluded: Any?
        ): String {
            return if (openAxisValueRestrictionPresent) {
                "$dimension($member[$hierarchy;$startingMember;$startingMemberIncluded])"
            } else {
                "$dimension($member)"
            }
        }
    }

    override fun validate(validationResults: ValidationResults) {

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = FinalOrdinateCategorisation::ordinateId
        )

        relationships.validate(validationResults)
    }
}
