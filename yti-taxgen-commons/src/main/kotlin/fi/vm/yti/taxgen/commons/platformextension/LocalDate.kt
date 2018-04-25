package fi.vm.yti.taxgen.commons.platformextension


import java.time.LocalDate
import kotlin.reflect.full.memberProperties


fun LocalDate.isBeforeOrEqualOrUndefined(other: LocalDate?): Boolean {
    if (other == null) {
        return true
    }

    if (this.isBefore(other) || this.isEqual(other)) {
        return true
    }

    return false
}

