package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropsLengths

data class Owner(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languageCodes: List<String>
) : Validatable {

    val languages: Set<Language> by lazy { languageCodes.mapNotNull { Language.findByIso6391Code(it) }.toSet() }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validatePropsLengths(
            validationResultBuilder = validationResultBuilder,
            properties = listOf(this::name, this::namespace, this::prefix, this::location, this::copyright),
            minLength = 2,
            maxLength = 500
        )

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::languageCodes,
            minLength = 1,
            maxLength = 10
        )

        validateIterableValuesUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = languageCodes,
            valueSelector = { it },
            valueName = Owner::languageCodes
        )

        validateCustom(
            validationResultBuilder = validationResultBuilder,
            valueName = Owner::languageCodes,
            validate = { errorReporter ->
                languageCodes.forEach { code ->
                    if (Language.findByIso6391Code(code) == null) {
                        errorReporter.error(
                            reason = "Unsupported language",
                            value = code
                        )
                    }
                }
            }
        )
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "DPM Owner",
            subjectIdentifiers = listOf(name)
        )
    }
}
