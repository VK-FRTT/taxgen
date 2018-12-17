package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner

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

    fun <R : Validatable?> extract(diagnosticContext: DiagnosticContextProvider, block: () -> R): R {
        return diagnostic.withContext(diagnosticContext) {
            val extractResult = block()

            val collector = ValidationCollector()
            extractResult?.validate(collector)

            diagnostic.validationResults(collector.compileResults())

            extractResult
        }
    }

    fun <R : Validatable> extractList(diagnosticContext: DiagnosticContextProvider, block: () -> List<R>): List<R> {
        return diagnostic.withContext(diagnosticContext) {
            val extractResult = block()

            val collector = ValidationCollector()
            extractResult.forEach { it.validate(collector) }

            diagnostic.validationResults(collector.compileResults())

            extractResult
        }
    }
}
