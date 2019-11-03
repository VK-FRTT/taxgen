package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.tables.OpenAxisValueRestrictionTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class OpenAxisValueRestrictionTransform(
    private val baselineRestrictions: List<BaselineOpenAxisValueRestriction>,
    private val diagnostic: Diagnostic
) {
    companion object {
        fun loadInitialState(diagnostic: Diagnostic): OpenAxisValueRestrictionTransform {

            val baselineRestrictions = transaction {
                OpenAxisValueRestrictionTable
                    .selectAll()
                    .map {
                        BaselineOpenAxisValueRestriction.fromRow(it)
                    }
            }

            baselineRestrictions.forEach {
                diagnostic.validate(it) {
                    ValidatableInfo(
                        objectKind = "BaselineOpenAxisValueRestriction",
                        objectAddress = "AxisID: ${it.axisId}"
                    )
                }
            }

            return OpenAxisValueRestrictionTransform(
                baselineRestrictions,
                diagnostic
            )
        }
    }

    fun transformAndWriteRestrictions() {
        val finalRestrictions = transaction {
            baselineRestrictions.map { baseline ->
                FinalOpenAxisValueRestriction.fromBaseline(
                    baseline
                )
            }
        }

        finalRestrictions.forEach {
            diagnostic.validate(it) {
                ValidatableInfo(
                    objectKind = "FinalOpenAxisValueRestriction",
                    objectAddress = "AxisID: ${it.axisId}"
                )
            }
        }

        transaction {
            OpenAxisValueRestrictionTable.deleteAll()
        }

        transaction {
            finalRestrictions.forEach {
                OpenAxisValueRestrictionTable.insertOpenAxisValueRestriction(
                    axisId = it.axisId,
                    hierarchyId = it.hierarchyId,
                    hierarchyStartingMemberId = it.hierarchyStartingMemberId,
                    isStartingMemberIncluded = it.isStartingMemberIncluded
                )
            }
        }
    }
}
