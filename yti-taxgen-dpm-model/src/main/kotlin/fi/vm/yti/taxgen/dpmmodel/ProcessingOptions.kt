package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import kotlin.reflect.KProperty0

data class ProcessingOptions(
    val sqliteDbMandatoryLabelTranslationLanguage: Language?,
    val sqliteDbMandatoryLabelTranslationSourceCandidateLanguages: List<Language>?,
    val sqliteDbDpmElementUriStorageLabelTranslationLanguage: Language?
) {
    companion object {
        fun empty() = ProcessingOptions(null, null, null)
    }

    fun emitDiagnostics(diagnostic: Diagnostic) {

        fun emitLanguageOption(property: KProperty0<Language?>) {
            diagnostic.info("ProcessingOption: ${property.name} [${property.get()?.iso6391Code ?: "-"}]")
        }

        fun emitLanguageListOption(property: KProperty0<List<Language>?>) {
            diagnostic.info("ProcessingOption: ${property.name} [${property.get()?.map { it.iso6391Code }?.joinToString() ?: "-"}]")
        }

        emitLanguageOption(this::sqliteDbMandatoryLabelTranslationLanguage)
        emitLanguageListOption(this::sqliteDbMandatoryLabelTranslationSourceCandidateLanguages)
        emitLanguageOption(this::sqliteDbDpmElementUriStorageLabelTranslationLanguage)
    }
}
