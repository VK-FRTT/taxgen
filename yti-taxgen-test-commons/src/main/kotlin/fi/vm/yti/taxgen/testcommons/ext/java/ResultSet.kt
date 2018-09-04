package fi.vm.yti.taxgen.testcommons.ext.java

import java.sql.ResultSet

fun ResultSet.toStringList(): List<String> {
    val rows = mutableListOf<String>()

    while (next()) {
        val rowValues = mutableListOf<String>()
        for (index in 1..metaData.columnCount) {
            rowValues.add(getString(index) ?: "nil")
        }

        rows.add(rowValues.joinToString())
    }
    return rows
}
