package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.tables.OpenAxisValueRestrictionTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
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

            diagnostic.haltIfUnrecoverableErrors {
                "OpenAxisValueRestriction baseline loading failed"
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

        diagnostic.haltIfUnrecoverableErrors {
            "OpenAxisValueRestriction transformation failed"
        }

        transaction {
            OpenAxisValueRestrictionTable.deleteAll()
        }

        transaction {
            finalRestrictions.forEach(::insertOpenAxisValueRestriction)
        }
    }

    private fun insertOpenAxisValueRestriction(restriction: FinalOpenAxisValueRestriction) {
        OpenAxisValueRestrictionTable.insert {
            it[axisIdCol] = restriction.axisId
            it[hierarchyIdCol] = restriction.hierarchyId
            it[hierarchyStartingMemberIdCol] = restriction.hierarchyStartingMemberId
            it[isStartingMemberIncluded] = restriction.isStartingMemberIncluded
        }
    }
}
