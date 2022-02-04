package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.openaxisvaluerestrictiontransform

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform.OrdinateCategorisationTransform
import fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.tablecelltransform.TableCellTransform

class FrameworksTransform(
    private val ordinateCategorisationTransform: OrdinateCategorisationTransform,
    private val openAxisValueRestrictionTransform: OpenAxisValueRestrictionTransform,
    private val tableCellTransform: TableCellTransform,
    private val diagnosticContext: DiagnosticContext
) {
    companion object {
        fun captureInitialState(diagnosticContext: DiagnosticContext): FrameworksTransform {
            return diagnosticContext.withContext(
                contextType = DiagnosticContexts.FrameworksTransformCaptureBaseline.toType(),
                contextDetails = null
            ) {
                doCaptureInitialState(
                    diagnosticContext = diagnosticContext
                )
            }
        }

        private fun doCaptureInitialState(diagnosticContext: DiagnosticContext): FrameworksTransform {

            val ordinateCategorisationTransform =
                OrdinateCategorisationTransform.loadInitialState(
                    diagnosticContext
                )

            val openAxisValueRestrictionTransform =
                OpenAxisValueRestrictionTransform.loadInitialState(
                    diagnosticContext
                )

            val tableCellTransform =
                TableCellTransform.loadInitialState(
                    diagnosticContext
                )

            return FrameworksTransform(
                ordinateCategorisationTransform,
                openAxisValueRestrictionTransform,
                tableCellTransform,
                diagnosticContext
            )
        }
    }

    fun updateFrameworkEntities() {
        diagnosticContext.runActionsWithContextTitle(
            contextType = DiagnosticContexts.FrameworksTransformUpdateEntities.toType(),
            actionsWithContextTitle = listOf(
                "OrdinateCategorisations" to { ordinateCategorisationTransform.transformAndWriteCategorisations() },
                "OpenAxisValueRestrictions" to { openAxisValueRestrictionTransform.transformAndWriteRestrictions() },
                "TableCells" to { tableCellTransform.transformAndWriteTableCells() }
            )
        )
    }
}
