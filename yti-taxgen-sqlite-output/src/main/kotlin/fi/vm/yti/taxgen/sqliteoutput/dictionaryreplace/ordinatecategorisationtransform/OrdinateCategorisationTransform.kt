package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.sqliteoutput.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class OrdinateCategorisationTransform(
    private val baselineCategorisations: List<BaselineOrdinateCategorisation>,
    private val diagnostic: Diagnostic
) {
    companion object {
        fun loadInitialState(diagnostic: Diagnostic): OrdinateCategorisationTransform {

            val baselineCategorisations = transaction {
                OrdinateCategorisationTable
                    .selectAll()
                    .map {
                        BaselineOrdinateCategorisation.fromRow(
                            it,
                            diagnostic
                        )
                    }
            }

            baselineCategorisations.forEach {
                diagnostic.validate(it) {
                    ValidatableInfo(
                        objectKind = "BaselineOrdinateCategorisation",
                        objectAddress = "OrdinateID: ${it.ordinateId}"
                    )
                }
            }

            return OrdinateCategorisationTransform(
                baselineCategorisations,
                diagnostic
            )
        }
    }

    fun transformAndWriteCategorisations() {
        val finalCategorisations = baselineCategorisations.map { baseline ->
            FinalOrdinateCategorisation.fromBaseline(
                baseline
            )
        }

        finalCategorisations.forEach {
            diagnostic.validate(it) {
                ValidatableInfo(
                    objectKind = "FinalOrdinateCategorisation",
                    objectAddress = "OrdinateID: ${it.ordinateId}"
                )
            }
        }

        transaction {
            OrdinateCategorisationTable.deleteAll()
        }

        transaction {
            finalCategorisations.forEach {
                OrdinateCategorisationTable.insertOrdinateCategorisation(
                    ordinateId = it.ordinateId,
                    dimensionId = it.dbReferences.dimensionId,
                    memberId = it.dbReferences.memberId,
                    dimensionMemberSignature = it.databaseIdSignature,
                    source = it.source,
                    dps = it.xbrlCodeSignature
                )
            }
        }
    }
}
