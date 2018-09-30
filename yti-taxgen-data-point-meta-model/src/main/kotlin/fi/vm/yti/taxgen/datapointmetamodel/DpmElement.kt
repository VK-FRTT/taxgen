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
            type = type,
            diagnosticLabel = concept.diagnosticLabel()
        )
    }

    fun diagnosticHandle(): String {
        return formatDiagnosticHandle(
            id = id,
            type = type,
            diagnosticLabel = concept.diagnosticLabel()
        )
    }

    companion object {
        fun typeName(kClass: KClass<*>): String {
            require(kClass.isSubclassOf(DpmElement::class), { "Expecting a DpmElement based class" })
            val name = kClass.simpleName ?: thisShouldNeverHappen("Anonymous DpmElement")
            return "$name" //TODO make names in diagnostic as "DPM Member"?
        }

        fun formatDiagnosticHandle(
            id: String,
            type: String,
            diagnosticLabel: String
        ): String {
            return if (diagnosticLabel.isNotEmpty()) {
                "$type: $diagnosticLabel [$id]"
            } else {
                "$type: [$id]"
            }
        }
    }
}
