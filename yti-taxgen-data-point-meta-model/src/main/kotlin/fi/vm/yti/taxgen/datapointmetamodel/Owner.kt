package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterablePropertyValuesUnique
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

    companion object : DiagnosticContextProvider {
        override fun contextType(): DiagnosticContextType = DiagnosticContextType.DpmOwner
        override fun contextName(): String = ""
        override fun contextRef(): String = ""
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

        validateIterablePropertyValuesUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = Owner::languages,
            valueProperty = Language::iso6391Code
        )
    }
}
