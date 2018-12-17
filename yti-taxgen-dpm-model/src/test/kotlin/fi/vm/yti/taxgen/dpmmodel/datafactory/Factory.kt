package fi.vm.yti.taxgen.dpmmodel.datafactory

import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

class Factory {

    private object Registry {
        private var definitions = HashSet<DataDefinition>()

        fun registerDefinitions(definitions: Set<DataDefinition>) {
            this.definitions.addAll(definitions)
        }

        fun definitionFor(kClass: KClass<*>): DataDefinition {
            return this.definitions.find { it.kClass == kClass }
                ?: throw NoSuchElementException("No test data definition found for: ${kClass.simpleName}")
        }
    }

    object Builder {
        private var dynamicAttributeContext = DynamicAttributeContext()

        fun attributesFor(kClass: KClass<*>, overrideAttributes: Map<String, Any?>?): MutableMap<String, Any?> {

            val definition = Registry.definitionFor(kClass)
            return buildOutgoingAttributes(definition.attributes, overrideAttributes)
        }

        private fun buildOutgoingAttributes(
            definedAttributes: Map<String, Any?>,
            overrideAttributes: Map<String, Any?>?
        ): MutableMap<String, Any?> {

            overrideAttributes?.forEach {
                check(definedAttributes.containsKey(it.key)) {
                    "Attribute override ${it.key} does not match any of defined attributes: ${definedAttributes.keys}"
                }
            }

            val outgoingAttributes = HashMap<String, Any?>()

            definedAttributes.forEach { (key, value) ->
                outgoingAttributes[key] =
                    if (overrideAttributes?.containsKey(key) == true) {
                        overrideAttributes[key]
                    } else if (value is DynamicAttributeDefinition) {
                        value.valueMaker.invoke(this.dynamicAttributeContext)
                    } else {
                        value
                    }
            }

            return outgoingAttributes
        }

        fun instantiate(kClass: KClass<*>, attributes: Map<String, Any?>): Any {
            val primaryConstructor: KFunction<*> = primaryConstructorFrom(kClass)

            val arguments = mapAttributesToFunctionParams(
                attributes,
                primaryConstructor.parameters,
                "${kClass.simpleName}.<PrimaryConstructor>"
            )

            return try {
                primaryConstructor.callBy(arguments)!!
            } catch (ex: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Instantiating ${kClass.simpleName} with args ${arguments.keys.map { it.name }} failed",
                    ex
                )
            }
        }

        private fun primaryConstructorFrom(kClass: KClass<*>): KFunction<*> {
            return kClass.primaryConstructor
                ?: throw UnsupportedOperationException("No primary constructor for ${kClass.qualifiedName}")
        }

        private fun mapAttributesToFunctionParams(
            attributes: Map<String, Any?>,
            parameters: List<KParameter>,
            functionName: String
        ): Map<KParameter, Any?> {

            //There must be 1:1 correspondence between attributes & function parameters
            val attributeKeys = attributes.keys
            val parameterNames = parameters.map { it.name }

            val surplusAttributes = attributeKeys - parameterNames
            val missingAttributes = parameterNames - attributeKeys

            require(surplusAttributes.isEmpty() && missingAttributes.isEmpty()) {
                "Provided attributes and function params do not match. " +
                    "Function: $functionName, " +
                    "Surplus attributes: $surplusAttributes, " +
                    "Missing attributes: $missingAttributes"
            }

            return parameters
                .associate { Pair(it, attributes[it.name]) }
        }
    }

    companion object {

        fun registerDefinitions(definitions: Set<DataDefinition>) {
            Registry.registerDefinitions(definitions)
        }

        inline fun <reified T : Any> attributesFor(overrides: Map<String, Any?>? = null): MutableMap<String, Any?> {
            val kClass = T::class
            return Builder.attributesFor(kClass, overrides)
        }

        inline fun <reified T : Any> instantiate(attributes: Map<String, Any?>? = null): T {
            val kClass = T::class

            if (attributes == null) {
                return Builder.instantiate(
                    kClass,
                    Builder.attributesFor(kClass, null)
                ) as T
            }

            return Builder.instantiate(
                kClass,
                attributes
            ) as T
        }

        inline fun <reified T : Any> instantiateWithOverrides(vararg overrides: Pair<String, Any?>): T {
            val kClass = T::class
            val attributes = Builder.attributesFor(kClass, overrides.toMap())

            return Builder.instantiate(
                kClass,
                attributes
            ) as T
        }
    }
}
