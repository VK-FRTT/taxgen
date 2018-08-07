package fi.vm.yti.taxgen.ycltodpmmapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrorCollector
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Owner

internal class DpmMappingContext private constructor(
    val diagnostic: Diagnostic,
    val owner: Owner,
    private val objectMapper: ObjectMapper
) {
    companion object {

        internal fun createRootContext(diagnostic: Diagnostic): DpmMappingContext {
            val rootOwner = Owner(
                name = "root",
                namespace = "",
                prefix = "",
                location = "",
                copyright = "",
                languages = Language.allLanguages(),
                defaultLanguage = Language.findByIso6391Code("en")!!
            )

            return DpmMappingContext(
                diagnostic = diagnostic,
                owner = rootOwner,
                objectMapper = JacksonObjectMapper.lenientObjectMapper()
            )
        }
    }

    internal fun cloneWithOwner(owner: Owner): DpmMappingContext {
        return DpmMappingContext(
            diagnostic = this.diagnostic,
            owner = owner,
            objectMapper = this.objectMapper
        )
    }

    fun <R : Validatable> extract(topicProvider: DiagnosticTopicProvider, block: () -> R): R {
        diagnostic.topicEnter(topicProvider)

        val ret = block()

        val validationErrors = ValidationErrorCollector()
        ret.validate(validationErrors)

        if (validationErrors.any()) {
            diagnostic.validationErrors(validationErrors)
        }

        diagnostic.topicExit()

        return ret
    }

    internal inline fun <reified T : Any> deserializeJson(jsonContent: String): T {
        return objectMapper.readValue(jsonContent) //TODO - handle mallformed JSON
    }
}
