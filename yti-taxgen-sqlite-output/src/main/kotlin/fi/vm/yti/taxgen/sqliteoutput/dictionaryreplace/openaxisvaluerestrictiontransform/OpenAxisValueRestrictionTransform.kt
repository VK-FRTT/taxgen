package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.sqliteoutput.tables.OpenAxisValueRestrictionTable
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

            diagnostic.validate(baselineRestrictions)

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

        diagnostic.validate(finalRestrictions)

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
