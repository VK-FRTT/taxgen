package fi.vm.yti.taxgen.commons.ext.kotlin

import kotlin.reflect.full.memberProperties

fun Any.getPropertyValue(propertyName: String): Any? {
    val property = this::class.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("Unknown property $propertyName for class ${this::class.simpleName}")

    return property.getter.call(this)
}

fun <T : Any> T?.whenNotNull(block: (T) -> Unit) {
    if (this != null){
        block(this)
    }
}
