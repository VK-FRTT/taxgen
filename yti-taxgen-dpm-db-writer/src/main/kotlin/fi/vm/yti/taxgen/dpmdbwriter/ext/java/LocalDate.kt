package fi.vm.yti.taxgen.dpmdbwriter.ext.java

fun java.time.LocalDate?.toJodaDateTimeOrNull(): org.joda.time.DateTime? {
    this ?: return null

    val jodaLocalDate = org.joda.time.LocalDate(
        this.year,
        this.monthValue,
        this.dayOfMonth
    )

    return org.joda.time.DateTime(
        jodaLocalDate.toDateTimeAtStartOfDay().millis)
}
