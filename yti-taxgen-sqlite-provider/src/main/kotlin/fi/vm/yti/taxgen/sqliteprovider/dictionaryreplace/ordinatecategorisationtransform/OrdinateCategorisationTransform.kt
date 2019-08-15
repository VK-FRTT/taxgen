package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.FixedEntitiesLookupItem
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

            diagnostic.haltIfUnrecoverableErrors {
                "OrdinateCategorisation baseline loading failed"
            }

            return OrdinateCategorisationTransform(
                baselineCategorisations,
                diagnostic
            )
        }
    }

    fun transformAndWriteCategorisations(
        dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>,
        fixedEntitiesLookupItem: FixedEntitiesLookupItem
    ) {
        // Transformation can be based on XBRL codes.
        // XBRL code contains owner prefix and thus is unique DB wide.
        //
        // Dimension XBRL Code: {owner.prefix}_dim:{dimension.dimensionCode}
        // Member XBRL Code: {owner.prefix}_{domain.domainCode}:{member.memberCode}

        val transformationContext =
            TransformationContext.fromLookupItems(dpmDictionaryLookupItems, fixedEntitiesLookupItem)

        val finalCategorisations = baselineCategorisations.map { baseline ->
            FinalOrdinateCategorisation.fromBaseline(
                baseline,
                transformationContext,
                diagnostic
            )
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
            it[dimensionIdCol] = categorisation.dimensionId
            it[memberIdCol] = categorisation.memberId
            it[dimensionMemberSignatureCol] = categorisation.databaseIdSignatureLiteral
            it[sourceCol] = categorisation.source
            it[dpsCol] = categorisation.xbrlCodeSignatureLiteral
        }
    }
}
