package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNonNull
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.EntityID

data class FinalOpenAxisValueRestriction(
    val restrictionStructure: OpenAxisValueRestrictionStructure,

    val axisId: EntityID<Int>?,
    val domainId: EntityID<Int>?,
    val hierarchyId: EntityID<Int>?,
    val hierarchyStartingMemberId: EntityID<Int>?,
    val isStartingMemberIncluded: Boolean?,

    val isStartingMemberPartOfHierarchy: Boolean
) : Validatable {

    companion object {

        fun fromBaseline(
            baseline: BaselineOpenAxisValueRestriction
        ): FinalOpenAxisValueRestriction {
            val (domainId, hierarchyId, hierarchyStartingMemberId) = resolveIds(baseline)

            return FinalOpenAxisValueRestriction(
                restrictionStructure = baseline.restrictionStructure,
                axisId = baseline.axisId,
                domainId = domainId,
                hierarchyId = hierarchyId,
                hierarchyStartingMemberId = hierarchyStartingMemberId,
                isStartingMemberIncluded = baseline.isStartingMemberIncluded,
                isStartingMemberPartOfHierarchy = resolveIsStartingMemberPartOfHierarchy(
                    hierarchyId,
                    hierarchyStartingMemberId
                )
            )
        }

        private fun resolveIds(
            baseline: BaselineOpenAxisValueRestriction
        ): Triple<EntityID<Int>?, EntityID<Int>?, EntityID<Int>?> {
            fun domainId(): EntityID<Int>? {
                val domainRow = baseline.domainXbrlCode?.let {
                    DomainTable.rowWhereDomainXbrlCode(it)
                }

                return domainRow?.get(DomainTable.id)
            }

            fun hierarchyId(domainId: EntityID<Int>?): EntityID<Int>? {
                val hierarchyRow = if (domainId != null && baseline.hierarchyCode != null) {
                    HierarchyTable.rowWhereDomainIdAndHierarchyCode(
                        domainId,
                        baseline.hierarchyCode
                    )
                } else {
                    null
                }

                return hierarchyRow?.get(HierarchyTable.id)
            }

            fun memberId(): EntityID<Int>? {
                val memberRow = baseline.startingMemberXbrlCode?.let {
                    MemberTable.rowWhereMemberXbrlCode(
                        baseline.startingMemberXbrlCode
                    )
                }

                return memberRow?.get(MemberTable.id)
            }

            val domainId = domainId()
            val hierarchyId = hierarchyId(domainId)
            val memberId = memberId()

            return Triple(domainId, hierarchyId, memberId)
        }

        private fun resolveIsStartingMemberPartOfHierarchy(
            hierarchyId: EntityID<Int>?,
            hierarchyStartingMemberId: EntityID<Int>?
        ): Boolean {
            val hierarchyStartingMemberNodeRow = if (hierarchyId != null && hierarchyStartingMemberId != null) {
                HierarchyNodeTable.rowWhereHierarchyIdAndMemberId(hierarchyId, hierarchyStartingMemberId)
            } else {
                null
            }

            return (hierarchyStartingMemberNodeRow != null)
        }
    }

    override fun validate(validationResults: ValidationResults) {
        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION ||
            restrictionStructure == OpenAxisValueRestrictionStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::axisId
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::domainId
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::hierarchyId
            )
        }

        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::hierarchyStartingMemberId
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::isStartingMemberIncluded
            )

            validateConditionTruthy(
                validationResults = validationResults,
                instance = this,
                property = FinalOpenAxisValueRestriction::isStartingMemberPartOfHierarchy,
                condition = { isStartingMemberPartOfHierarchy },
                message = { "is not part of hierarchy" }
            )
        }
    }
}
