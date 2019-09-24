package fi.vm.yti.taxgen.rdsprovider.configinput

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import kotlin.reflect.KProperty0

fun <T : Any?> validateValueNotNull(property: KProperty0<T>, diagnostic: Diagnostic) {
    if (property.get() == null) {
        diagnostic.fatal("No value for '${property.name}'")
    }
}

fun <T : List<*>?> validateListElementsNotNull(property: KProperty0<T>, diagnostic: Diagnostic) {
    property.get()?.forEach {
        if (it == null) {
            diagnostic.fatal("NULL element in '${property.name}'")
        }
    }
}
