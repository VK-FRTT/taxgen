package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface DpmElement : Validatable {
    val uri: String
    val concept: Concept
    val type: String
        get() = typeName(this::class)

    fun validateDpmElement(
        validationResults: ValidationResults,
        minLabelLangCount: Int = 1
    ) {

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = DpmElement::uri,
            minLength = 1,
            maxLength = 500
        )

        concept.validateConcept(
            validationResults,
            minLabelLangCount
        )
    }

    fun code(): String = ""

    fun validationContextInfo(): ValidatableInfo {
        return ValidatableInfo(
            objectKind = typeName(this::class),
            objectAddress = uri
        )
    }

    companion object {
        fun typeName(kClass: KClass<*>): String {

            require(kClass.isSubclassOf(DpmElement::class), { "Expecting a DpmElement based class" })

            return kClass.simpleName ?: thisShouldNeverHappen("Anonymous DpmElement")
        }
    }
}
