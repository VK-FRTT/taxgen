package fi.vm.yti.taxgen.commons.platformextension

import kotlin.reflect.full.memberProperties


fun Any.getPropertyValue(propertyName: String): Any? {
    val property = this::class.memberProperties.find { it.name == propertyName }
            ?: throw IllegalArgumentException("Unknown property $propertyName for class ${this::class.simpleName}")

    return property.getter.call(this)
}
