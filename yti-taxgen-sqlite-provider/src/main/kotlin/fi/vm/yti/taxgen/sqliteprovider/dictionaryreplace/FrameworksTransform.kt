package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform.OrdinateCategorisationTransform
import fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.tablecelltransform.TableCellTransform

class FrameworksTransform(
    private val ordinateCategorisationTransform: OrdinateCategorisationTransform,
    private val openAxisValueRestrictionTransform: OpenAxisValueRestrictionTransform,
    private val tableCellTransform: TableCellTransform
) {
    companion object {
        fun loadInitialState(diagnostic: Diagnostic): FrameworksTransform {

            val ordinateCategorisationTransform =
                OrdinateCategorisationTransform.loadInitialState(
                    diagnostic
                )

            val openAxisValueRestrictionTransform =
                OpenAxisValueRestrictionTransform.loadInitialState(
                    diagnostic
                )

            val tableCellTransform =
                TableCellTransform.loadInitialState(
                    diagnostic
                )

            return FrameworksTransform(
                ordinateCategorisationTransform,
                openAxisValueRestrictionTransform,
                tableCellTransform
            )
        }
    }

    fun transformFrameworkEntities() {
        ordinateCategorisationTransform.transformAndWriteCategorisations()
        openAxisValueRestrictionTransform.transformAndWriteRestrictions()
        tableCellTransform.transformAndWriteTableCells()
    }
}
