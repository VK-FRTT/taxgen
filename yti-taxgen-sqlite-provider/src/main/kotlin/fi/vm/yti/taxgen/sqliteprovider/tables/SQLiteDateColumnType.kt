package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormatterBuilder

/**
 * Custom date column implementation to overcome Exposed bug
 * https://github.com/JetBrains/Exposed/issues/318
 */
class SQLiteDateColumnType(private val withTime: Boolean) : ColumnType() {

    // YYYY-MM-dd
    private val SQLITE_DATE_STRING_FORMATTER =
        DateTimeFormatterBuilder()
            .appendYear(4, 9)
            .appendLiteral('-')
            .appendMonthOfYear(2)
            .appendLiteral('-')
            .appendDayOfMonth(2)
            .toFormatter()

    // YYYY-MM-dd HH:mm:ssz
    private val SQLITE_DATE_TIME_STRING_FORMATTER =
        DateTimeFormatterBuilder()
            .appendYear(4, 9)
            .appendLiteral('-')
            .appendMonthOfYear(2)
            .appendLiteral('-')
            .appendDayOfMonth(2)
            .appendLiteral(' ')
            .appendHourOfDay(2)
            .appendLiteral(':')
            .appendMinuteOfHour(2)
            .appendLiteral(':')
            .appendSecondOfMinute(2)
            .appendTimeZoneOffset("Z", true, 2, 4)
            .toFormatter()

    override fun sqlType(): String = "DATE"

    override fun nonNullValueToString(value: Any): String {
        return when (value) {
            is String -> {
                value
            }
            is DateTime -> {
                val valueString = if (withTime) {
                    SQLITE_DATE_TIME_STRING_FORMATTER.print(value)
                } else {
                    SQLITE_DATE_STRING_FORMATTER.print(value)
                }

                "'$valueString'"
            }

            else -> {
                thisShouldNeverHappen("Unexpected value: $value of ${value::class.qualifiedName}")
            }
        }
    }

    override fun valueFromDB(value: Any): Any {
        return when (value) {
            is String -> {
                if (withTime) {
                    SQLITE_DATE_TIME_STRING_FORMATTER.parseDateTime(value)
                } else {
                    SQLITE_DATE_STRING_FORMATTER.parseDateTime(value)
                }
            }
            else -> {
                value
            }
        }
    }

    override fun notNullValueToDB(value: Any): Any {
        return when (value) {
            is DateTime -> {
                if (withTime) {
                    SQLITE_DATE_TIME_STRING_FORMATTER.print(value)
                } else {
                    SQLITE_DATE_STRING_FORMATTER.print(value)
                }
            }
            else -> {
                value
            }
        }
    }
}

fun Table.sqliteDate(name: String): Column<DateTime> =
    registerColumn(name, SQLiteDateColumnType(false))

fun Table.sqliteDateTime(name: String): Column<DateTime> =
    registerColumn(name, SQLiteDateColumnType(true))
