package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNonBlank
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateNonNull
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.OpenAxisValueRestrictionTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOpenAxisValueRestriction(
    val restrictionStructure: OpenAxisValueRestrictionStructure,

    val axisId: EntityID<Int>?,

    val domainXbrlCode: String?,
    val hierarchyCode: String?,
    val startingMemberXbrlCode: String?,

    val isStartingMemberIncluded: Boolean?,

    val isStartingMemberPartOfHierarchy: Boolean

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
                startingMemberXbrlCode = hierarchyStartingMemberRow?.get(MemberTable.memberXBRLCodeCol),
                isStartingMemberIncluded = openAxisValueRestrictionRow[OpenAxisValueRestrictionTable.isStartingMemberIncluded],
                isStartingMemberPartOfHierarchy = (hierarchyStartingMemberNodeRow != null)
            )
        }
    }

    override fun validate(validationResults: ValidationResults) {
        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION ||
            restrictionStructure == OpenAxisValueRestrictionStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION
        ) {
            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::axisId
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::domainXbrlCode
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::hierarchyCode
            )
        }

        if (restrictionStructure == OpenAxisValueRestrictionStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION) {
            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::startingMemberXbrlCode
            )

            validateNonNull(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::isStartingMemberIncluded
            )

            validateConditionTruthy(
                validationResults = validationResults,
                instance = this,
                property = BaselineOpenAxisValueRestriction::isStartingMemberPartOfHierarchy,
                condition = { isStartingMemberPartOfHierarchy },
                message = { "is not part of hierarchy" }
            )
        }
    }
}
