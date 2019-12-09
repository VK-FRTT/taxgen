package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import java.sql.ResultSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DpmDbWriter_DictionaryReplace_TableCellTransform_ModuleTest :
    DpmDbWriter_DictionaryReplaceModuleTestBase() {

    @Test
    fun `TableCell signature values should get NULL`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mTableCell(CellID, TableID, IsRowKey, IsShaded, BusinessCode, DatapointSignature, DPS)
            VALUES (100, 200, 0, 0, "BizCode", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)")
            """.trimIndent()
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = readAllTableCells()

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#CellID, #TableID, #IsRowKey, #IsShaded, #BusinessCode, #DatapointSignature, #DPS",
            "100, 200, 0, 0, BizCode, nil, nil"
        )
    }

    private fun readAllTableCells(): ResultSet {
        return outputDbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mTableCell
            """.trimIndent()
        )
    }
}
