package fi.vm.yti.taxgen.testcommons.ext.java

import java.sql.ResultSet

fun ResultSet.toStringList(includeHeader: Boolean = true): List<String> {
    val rows = mutableListOf<String>()

    val md = metaData

    if (includeHeader) {
        val headerValues = mutableListOf<String>()
        for (index in 1..md.columnCount) {
            headerValues.add(md.getColumnName(index))
        }
        rows.add(headerValues.joinToString())
    }

    while (next()) {
        val rowValues = mutableListOf<String>()
        for (index in 1..md.columnCount) {
            rowValues.add(getString(index) ?: "nil")
        }

        rows.add(rowValues.joinToString())
    }
    return rows
}
