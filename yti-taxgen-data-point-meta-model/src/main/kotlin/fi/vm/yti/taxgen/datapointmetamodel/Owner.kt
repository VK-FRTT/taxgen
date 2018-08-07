package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableElementsUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class Owner(
    val name: String,
    val namespace: String,
    val prefix: String,
    val location: String,
    val copyright: String,
    val languages: Set<Language>,
    val defaultLanguage: Language
) : Validatable {

    companion object : DiagnosticTopicProvider {
        override fun topicType(): String = "Owner"
        override fun topicName(): String = ""
        override fun topicIdentifier(): String = ""
    }

    override fun validate(validationErrors: ValidationErrors) {

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::name,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::namespace,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::prefix,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::location,
            minLength = 2,
            maxLength = 100
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::copyright,
            minLength = 2,
            maxLength = 1000
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::languages,
            minLength = 1,
            maxLength = 10
        )

        validateIterableElementsUnique(
            validationErrors = validationErrors,
            instance = this,
            property = Owner::languages,
            keySelector = { it.iso6391Code }
        )
    }
}
