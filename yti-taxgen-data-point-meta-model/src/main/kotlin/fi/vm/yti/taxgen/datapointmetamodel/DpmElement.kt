package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

interface DpmElement : Validatable {
    val id: String
    val concept: Concept
    val type: String
        get() = typeName(this::class)

    override fun validate(validationErrors: ValidationErrors) {

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = DpmElement::id,
            minLength = 1,
            maxLength = 128
        )

        concept.validate(validationErrors)
    }

    fun ref(): DpmElementRef {
        return DpmElementRef(
            id = id,
            type = type
        )
    }

    companion object {
        fun typeName(kClass: KClass<*>): String {
            require(kClass.isSubclassOf(DpmElement::class), { "Expecting a DpmElement based class" })
            return kClass.simpleName ?: thisShouldNeverHappen("Anonymous DpmElement")
        }
    }
}
