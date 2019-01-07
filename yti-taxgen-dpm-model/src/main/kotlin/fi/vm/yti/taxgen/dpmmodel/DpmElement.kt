package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface DpmElement : Validatable {
    val uri: String
    val concept: Concept
    val type: String
        get() = typeName(this::class)

    override fun validate(validationResults: ValidationResults) {

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = DpmElement::uri,
            minLength = 1,
            maxLength = 128
        )

        concept.validate(validationResults)
    }

    fun ref(): DpmElementRef = DpmElementRef(
        uri = uri,
        type = type,
        diagnosticLabel = concept.diagnosticLabel()
    )

    companion object {
        fun typeName(kClass: KClass<*>): String {

            require(kClass.isSubclassOf(DpmElement::class), { "Expecting a DpmElement based class" })

            val name = kClass.simpleName ?: thisShouldNeverHappen("Anonymous DpmElement")

            return "$name"
        }
    }
}
