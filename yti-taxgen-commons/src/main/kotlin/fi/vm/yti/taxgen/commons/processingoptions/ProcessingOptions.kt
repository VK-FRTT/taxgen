package fi.vm.yti.taxgen.commons.processingoptions

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import kotlin.reflect.KProperty0

data class ProcessingOptions(
    val diagnosticSourceLanguages: List<Language>,

    val sqliteDbDpmElementInherentTextLanguage: Language?,

    val sqliteDbMandatoryLabelLanguage: Language?,
    val sqliteDbMandatoryLabelSourceLanguages: List<Language>?,

    val sqliteDbDpmElementUriStorageLabelLanguage: Language?,

    val sqliteDbHierarchyNodeLabelCompositionLanguages: List<Language>?,
    val sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage: Language?
) {
    fun emitDiagnostics(diagnostic: Diagnostic) {

        fun emitLanguageOption(property: KProperty0<Language?>) {
            diagnostic.info("ProcessingOption: ${property.name} [${property.get()?.iso6391Code ?: "-"}]")
        }

        fun emitLanguageListOption(property: KProperty0<List<Language>?>) {
            diagnostic.info(
                "ProcessingOption: ${property.name} [${property.get()?.map { it.iso6391Code }?.joinToString() ?: "-"}]"
            )
        }

        emitLanguageListOption(this::diagnosticSourceLanguages)

        emitLanguageOption(this::sqliteDbDpmElementInherentTextLanguage)

        emitLanguageOption(this::sqliteDbMandatoryLabelLanguage)
        emitLanguageListOption(this::sqliteDbMandatoryLabelSourceLanguages)

        emitLanguageOption(this::sqliteDbDpmElementUriStorageLabelLanguage)

        emitLanguageListOption(this::sqliteDbHierarchyNodeLabelCompositionLanguages)
        emitLanguageOption(this::sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage)
    }
}
