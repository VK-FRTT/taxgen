package fi.vm.yti.taxgen.datapointmetamodel.testdataframework

import kotlin.reflect.KClass

class DataDefinition(
    val kClass: KClass<*>,
    val attributes: Map<String, Any?>
)
