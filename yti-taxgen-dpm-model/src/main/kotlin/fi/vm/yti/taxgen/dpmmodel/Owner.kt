package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Owner(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languages: Set<Language>,
    val defaultLanguage: Language
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::name,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::namespace,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::prefix,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::location,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::copyright,
            minLength = 2,
            maxLength = 1000
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Owner::languages,
            minLength = 1,
            maxLength = 10
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "languages",
            iterable = languages,
            valueSelector = { it.iso6391Code },
            valueDescription = "iso6391Code"
        )
    }
}
