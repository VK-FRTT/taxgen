package fi.vm.yti.taxgen.datapointmetamodel.datafactory

class DynamicAttributeContext {

    inline fun <reified T : Any> instantiate(): T {
        val attributes = Factory.attributesFor<T>()
        return Factory.instantiate(attributes)
    }
}
