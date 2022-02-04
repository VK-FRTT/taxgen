package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNullAndNonBlank
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import fi.vm.yti.taxgen.sqliteoutput.tables.OpenAxisValueRestrictionTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOpenAxisValueRestriction(
    val restrictionStructure: OpenAxisValueRestrictionStructure,

    val axisId: EntityID<Int>?,

    val domainXbrlCode: String?,
    val hierarchyCode: String?,
    val hierarchyStartingMemberXbrlCode: String?,

    val isStartingMemberIncluded: Boolean?,

    val isHierarchyStartingMemberPartOfHierarchy: Boolean

) : Validatable {

    companion object {

        fun fromRow(
            openAxisValueRestrictionRow: ResultRow
        ): BaselineOpenAxisValueRestriction {

            val restrictionStructure =
                if (openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.hierarchyStartingMemberIdCol] != null) {
                    OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION
                } else {
                    OpenAxisValueRestrictionStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
                }

            val hierarchyId = openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.hierarchyIdCol]
            val hierarchyRow = hierarchyId?.let { HierarchyTable.rowWhereHierarchyId(it) }

            val hierarchyStartingMemberId =
                openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.hierarchyStartingMemberIdCol]
            val hierarchyStartingMemberRow = hierarchyStartingMemberId?.let { MemberTable.rowWhereMemberId(it) }

            val hierarchyStartingMemberNodeRow = if (hierarchyId != null && hierarchyStartingMemberId != null) {
                HierarchyNodeTable.rowWhereHierarchyIdAndMemberId(hierarchyId, hierarchyStartingMemberId)
            } else {
                null
            }

            val domainId = hierarchyRow?.let { it[HierarchyTable.domainIdCol] }
            val domainRow = domainId?.let { DomainTable.rowWhereDomainId(it) }

            return BaselineOpenAxisValueRestriction(
                restrictionStructure = restrictionStructure,
                axisId = openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.axisIdCol],
                domainXbrlCode = domainRow?.get(DomainTable.domainXBRLCodeCol),
                hierarchyCode = hierarchyRow?.get(HierarchyTable.hierarchyCodeCol),
                hierarchyStartingMemberXbrlCode = hierarchyStartingMemberRow?.get(MemberTable.memberXBRLCodeCol),
                isStartingMemberIncluded = openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.isStartingMemberIncludedCol],
                isHierarchyStartingMemberPartOfHierarchy = (hierarchyStartingMemberNodeRow != null)
            )
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

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::domainXbrlCode
            )

            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyCode
            )
        }

        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonNullAndNonBlank(
                validationResultBuilder = validationResultBuilder,
                property = this::hierarchyStartingMemberXbrlCode
            )

            validateNonNull(
                validationResultBuilder = validationResultBuilder,
                property = this::isStartingMemberIncluded
            )

            validatePropFulfillsCondition(
                validationResultBuilder = validationResultBuilder,
                property = this::isHierarchyStartingMemberPartOfHierarchy,
                condition = { it },
                reason = { "HierarchyStartingMember ($hierarchyStartingMemberXbrlCode) is not part of Hierarchy ($hierarchyCode)" },
                includeValueToError = false
            )
        }
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OpenAxisValueRestriction (baseline)",
            subjectIdentifiers = listOf("AxisID: $axisId")
        )
    }
}
