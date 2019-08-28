package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.tablecelltransform

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
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
