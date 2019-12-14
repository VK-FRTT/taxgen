package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength

interface DpmElement : Validatable {
    val uri: String
    val concept: Concept

    fun validateDpmElement(
        validationResultBuilder: ValidationResultBuilder,
        minLabelLangCount: Int = 1
    ) {

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::uri,
            minLength = 1,
            maxLength = 500
        )

        validationResultBuilder.withNestedObject(Concept::class) {
            concept.validateConcept(
                validationResultBuilder,
                minLabelLangCount
            )
        }
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = this::class.simpleName ?: throwIllegalDpmModelState("Anonymous DpmElement"),
            subjectIdentifier = uri
        )
    }

    fun code(): String = ""
}
