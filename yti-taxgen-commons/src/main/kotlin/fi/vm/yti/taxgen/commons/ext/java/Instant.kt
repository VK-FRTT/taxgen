package fi.vm.yti.taxgen.commons.ext.java

import java.time.Instant

fun Instant.isBeforeOrEqual(other: Instant): Boolean {
    if (this.isBefore(other) || this.equals(other)) {
        return true
    }

    return false
}
