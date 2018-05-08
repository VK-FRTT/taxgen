package fi.vm.yti.taxgen.commons.ext.java

import java.time.LocalDate

fun LocalDate.isBeforeOrEqualOrUndefined(other: LocalDate?): Boolean {
    if (other == null) {
        return true
    }

    if (this.isBefore(other) || this.isEqual(other)) {
        return true
    }

    return false
}
