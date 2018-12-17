package fi.vm.yti.taxgen.sqliteprovider.ext.java

fun java.time.Instant.toJodaDateTime(): org.joda.time.DateTime {
    return org.joda.time.DateTime(
        this.toEpochMilli(),
        org.joda.time.DateTimeZone.UTC
    )
}
