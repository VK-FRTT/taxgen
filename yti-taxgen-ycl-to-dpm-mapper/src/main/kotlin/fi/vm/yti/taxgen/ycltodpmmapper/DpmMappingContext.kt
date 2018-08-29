package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrorCollector
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Owner

internal class DpmMappingContext private constructor(
    val diagnostic: Diagnostic,
    val owner: Owner
) {
    companion object {

        internal fun createRootContext(diagnostic: Diagnostic): DpmMappingContext {
            val langEn = Language.findByIso6391Code("en")!!

            val rootOwner = Owner(
                name = "root",
                namespace = "",
                prefix = "",
                location = "",
                copyright = "",
                languages = setOf(langEn),
                defaultLanguage = langEn
            )

            return DpmMappingContext(
                diagnostic = diagnostic,
                owner = rootOwner
            )
        }
    }

    internal fun cloneWithOwner(owner: Owner): DpmMappingContext {
        return DpmMappingContext(
            diagnostic = this.diagnostic,
            owner = owner
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

    fun <R : Validatable> extractList(topicProvider: DiagnosticTopicProvider, block: () -> List<R>): List<R> {
        diagnostic.topicEnter(topicProvider)

        val ret = block()

        val validationErrors = ValidationErrorCollector()

        ret.forEach { it.validate(validationErrors) }

        if (validationErrors.any()) {
            diagnostic.validationErrors(validationErrors)
        }

        diagnostic.topicExit()

        return ret
    }

    internal inline fun <reified T : Any> deserializeJson(jsonContent: String): T {
        return JsonOps.readValue(jsonContent, diagnostic)
    }
}
