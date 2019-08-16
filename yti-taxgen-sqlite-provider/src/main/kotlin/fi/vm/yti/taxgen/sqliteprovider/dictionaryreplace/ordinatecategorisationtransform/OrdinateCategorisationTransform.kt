package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
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

                //TODO
                //validateSignatureRelationEquality(
                //    it.databaseIdSignature,
                //    it.xbrlCodeSignature
                //)
            }

            diagnostic.haltIfUnrecoverableErrors {
                "OrdinateCategorisation baseline loading failed"
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

        diagnostic.haltIfUnrecoverableErrors {
            "OrdinateCategorisation transformation failed"
        }

        transaction {
            OrdinateCategorisationTable.deleteAll()
        }

        transaction {
            finalCategorisations.forEach(::insertOrdinateCategorisation)
        }
    }

    private fun insertOrdinateCategorisation(categorisation: FinalOrdinateCategorisation) {
        OrdinateCategorisationTable.insert {
            it[ordinateIdCol] = categorisation.ordinateId
            it[dimensionIdCol] = categorisation.relationships.dimensionId
            it[memberIdCol] = categorisation.relationships.memberId
            it[dimensionMemberSignatureCol] = categorisation.databaseIdSignature
            it[sourceCol] = categorisation.source
            it[dpsCol] = categorisation.xbrlCodeSignature
        }
    }
}
