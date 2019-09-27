package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import kotlin.reflect.KProperty0

data class ProcessingOptions(
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
            diagnostic.info("ProcessingOption: ${property.name} [${property.get()?.map { it.iso6391Code }?.joinToString() ?: "-"}]")
        }

        emitLanguageOption(this::sqliteDbDpmElementInherentTextLanguage)

        emitLanguageOption(this::sqliteDbMandatoryLabelLanguage)
        emitLanguageListOption(this::sqliteDbMandatoryLabelSourceLanguages)

        emitLanguageOption(this::sqliteDbDpmElementUriStorageLabelLanguage)

        emitLanguageListOption(this::sqliteDbHierarchyNodeLabelCompositionLanguages)
        emitLanguageOption(this::sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage)
    }
}
