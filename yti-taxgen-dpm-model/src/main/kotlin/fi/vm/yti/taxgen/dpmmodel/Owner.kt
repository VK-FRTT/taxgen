package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class Owner(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languageCodes: List<String>,
    val defaultLanguageCode: String
) : Validatable {

    val languages: Set<Language> by lazy { languageCodes.mapNotNull { Language.findByIso6391Code(it) }.toSet() }
    val defaultLanguage: Language by lazy { Language.findByIso6391Code(defaultLanguageCode)!! }

    override fun validate(validationResults: ValidationResults) {

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(Owner::name, Owner::namespace, Owner::prefix, Owner::location, Owner::copyright),
            minLength = 2,
            maxLength = 500
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::languageCodes,
            minLength = 1,
            maxLength = 10
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "languages",
            iterable = languageCodes,
            valueSelector = { it },
            valueDescription = "language code"
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "languages",
            validate = { messages ->
                languageCodes.forEach { code ->
                    if (Language.findByIso6391Code(code) == null) {
                        messages.add("unsupported language '$code'")
                    }
                }
            }
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "defaultLanguage",
            validate = { messages ->
                if (Language.findByIso6391Code(defaultLanguageCode) == null) {
                    messages.add("unsupported default language '$defaultLanguageCode'")
                }
            }
        )
    }
}
