package fi.vm.yti.taxgen.commons.datavalidation

import kotlin.reflect.KProperty1

class ValidationErrors {

    fun <T : Any, P : Any> add(instance: T, property: KProperty1<T, P>, message: String) {
    }

    fun any(): Boolean {
        return false
    }
}
