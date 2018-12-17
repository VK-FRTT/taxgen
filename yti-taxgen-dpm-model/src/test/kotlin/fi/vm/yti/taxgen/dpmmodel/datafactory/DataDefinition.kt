package fi.vm.yti.taxgen.dpmmodel.datafactory

import kotlin.reflect.KClass

class DataDefinition(
    val kClass: KClass<*>,
    val attributes: Map<String, Any?>
)
