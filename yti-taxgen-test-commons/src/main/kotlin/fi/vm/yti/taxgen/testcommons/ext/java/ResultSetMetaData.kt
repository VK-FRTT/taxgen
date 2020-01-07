package fi.vm.yti.taxgen.testcommons.ext.java

import java.sql.ResultSetMetaData
import java.sql.ResultSetMetaData.columnNoNulls
import java.sql.ResultSetMetaData.columnNullable
import java.sql.ResultSetMetaData.columnNullableUnknown

fun ResultSetMetaData.columnConfigs(): List<String> {
    return (0..columnCount).map { columnConfigToString(it) }
}

private fun ResultSetMetaData.columnConfigToString(column: Int): String {
    val separator = ", "
    val sb = StringBuilder()

    if (column == 0) {
        sb.append("#ColumnName")
        sb.append(separator)

        sb.append("#ColumnType")
        sb.append(separator)

        sb.append("#Nullable")
        sb.append(separator)

        sb.append("#AutoIncrement")
    } else {
        sb.append(getColumnName(column))
        sb.append(separator)

        val type = getColumnType(column)
        val typeName = when (type) {
            java.sql.Types.INTEGER -> "INTEGER"
            java.sql.Types.VARCHAR -> "VARCHAR"
            java.sql.Types.DATE -> "DATE"
            java.sql.Types.BOOLEAN -> "BOOLEAN"
            else -> "UNMAPPED: $type"
        }

        sb.append(typeName)
        sb.append(separator)

        when (isNullable(column)) {
            columnNoNulls -> sb.append("NonNullable")
            columnNullable -> sb.append("Nullable")
            columnNullableUnknown -> sb.append("NullableUnknown")
        }
        sb.append(separator)

        sb.append(isAutoIncrement(column))
    }

    return sb.toString()
}
