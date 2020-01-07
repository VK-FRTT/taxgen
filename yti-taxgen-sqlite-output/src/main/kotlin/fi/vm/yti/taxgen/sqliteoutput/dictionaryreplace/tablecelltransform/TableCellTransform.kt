package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.tablecelltransform

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction

class TableCellTransform(
    private val diagnostic: Diagnostic
) {
    companion object {
        fun loadInitialState(diagnostic: Diagnostic): TableCellTransform {

            return TableCellTransform(
                diagnostic
            )
        }
    }

    fun transformAndWriteTableCells() {
        transaction {
            TransactionManager.current()
                .connection
                .createStatement()
                .executeUpdate(
                    """
                        UPDATE mTableCell SET
                            DataPointSignature = null,
                            DPS = null
                        """.trimIndent()
                )
        }
    }
}