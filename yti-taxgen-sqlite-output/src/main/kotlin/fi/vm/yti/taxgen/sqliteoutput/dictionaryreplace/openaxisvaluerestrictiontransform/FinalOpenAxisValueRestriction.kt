package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import org.jetbrains.exposed.dao.id.EntityID

data class FinalOpenAxisValueRestriction(
    val restrictionStructure: OpenAxisValueRestrictionStructure,

    val axisId: EntityID<Int>?,
    val domainId: EntityID<Int>?,
    val hierarchyId: EntityID<Int>?,
    val hierarchyStartingMemberId: EntityID<Int>?,
    val isStartingMemberIncluded: Boolean?,

    val isHierarchyStartingMemberPartOfHierarchy: Boolean
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
                isHierarchyStartingMemberPartOfHierarchy = isHierarchyStartingMemberPartOfHierarchy(
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
                val memberRow = baseline.hierarchyStartingMemberXbrlCode?.let {
                    MemberTable.rowWhereMemberXbrlCode(
                        baseline.hierarchyStartingMemberXbrlCode
                    )
                }

                return memberRow?.get(MemberTable.id)
            }

            val domainId = domainId()
            val hierarchyId = hierarchyId(domainId)
            val memberId = memberId()

            return Triple(domainId, hierarchyId, memberId)
        }

        private fun isHierarchyStartingMemberPartOfHierarchy(
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

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION ||
            restrictionStructure == OpenAxisValueRestrictionStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::axisId
            )

            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::domainId
            )

            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyId
            )
        }

        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberId
            )

            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::isStartingMemberIncluded
            )

            validatePropFulfillsCondition(
                validationResultBuilder = validationResultBuilder,
                property = this::isHierarchyStartingMemberPartOfHierarchy,
                condition = { it },
                reason = { "HierarchyStartingMember (ID $hierarchyStartingMemberId) is not part of Hierarchy (ID $hierarchyId)" },
                includeValueToError = false
            )
        }
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OpenAxisValueRestriction (transformed)",
            subjectIdentifiers = listOf("AxisID: $axisId")
        )
    }
}
