package fi.vm.yti.taxgen.datapointmetamodel.validationfw

import fi.vm.yti.taxgen.commons.ext.kotlin.getPropertyValue

class DataValidationException(override var message: String) : Exception(message)
class DataValidationConfigurationError(override var message: String) : Error(message)

fun validateProperty(
    instance: Any,
    property: String,
    minLength: Int? = null,
    maxLength: Int? = null
) {
    val value = instance.getPropertyValue(property)
    val messages = mutableListOf<String>()

    if (minLength != null && value != null) {
        doValidate("minLength", property, value, minLength, messages)
    }

    if (maxLength != null && value != null) {
        doValidate("maxLength", property, value, maxLength, messages)
    }

    if (!messages.isEmpty()) {
        throw DataValidationException("[${messages.joinToString()}]")
    }
}

private fun doValidate(
    validatorType: String,
    propertyName: String,
    value: Any,
    limit: Int,
    messages: MutableList<String>
) {
    if (validatorType == "minLength") {
        when (value) {
            is String -> {
                if (value.length < limit) {
                    messages.add("$propertyName: $validatorType $limit")
                }
            }

            is List<*> -> {
                if (value.size < limit) {
                    messages.add("$propertyName: $validatorType $limit")
                }
            }

            else -> unsupportedDataTypeForValidator(validatorType, propertyName, value)
        }
    }

    if (validatorType == "maxLength") {
        when (value) {
            is String -> {
                if (value.length > limit) {
                    messages.add("$propertyName: $validatorType $limit")
                }
            }

            is List<*> -> {
                if (value.size > limit) {
                    messages.add("$propertyName: $validatorType $limit")
                }
            }

            else -> unsupportedDataTypeForValidator(validatorType, propertyName, value)
        }
    }
}

private fun unsupportedDataTypeForValidator(validatorType: String, propertyName: String, value: Any) {
    throw DataValidationConfigurationError("$validatorType validator: Unsupported data type ${value::class.simpleName} " +
            "in property $propertyName for " )
}
