package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength

data class TypedDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val dataType: String
) : DpmElement {

    companion object {
        val VALID_DATA_TYPES = listOf(
            "Boolean",
            "Date",
            "Integer",
            "Monetary",
            "Percent",
            "String",
            "Decimal",
            "Lei",
            "Isin"
        )
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::dataType,
            condition = { VALID_DATA_TYPES.contains(it) },
            reason = { "Unsupported value" }
        )
    }

    override fun code(): String = domainCode
}
