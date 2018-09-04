package fi.vm.yti.taxgen.testcommons.ext.java

import java.sql.ResultSetMetaData
import java.sql.ResultSetMetaData.columnNoNulls
import java.sql.ResultSetMetaData.columnNullable
import java.sql.ResultSetMetaData.columnNullableUnknown

fun ResultSetMetaData.columnConfigToString(column: Int): String {
    val separator = ", "
    val sb = StringBuilder()

    sb.append(getColumnName(column))
    sb.append(separator)

    sb.append(getColumnTypeName(column))
    sb.append(separator)

    when (isNullable(column)) {
        columnNoNulls -> sb.append("NonNullable")
        columnNullable -> sb.append("Nullable")
        columnNullableUnknown -> sb.append("NullableUnknown")
    }
    sb.append(separator)

    sb.append(isAutoIncrement(column))

    return sb.toString()
}
