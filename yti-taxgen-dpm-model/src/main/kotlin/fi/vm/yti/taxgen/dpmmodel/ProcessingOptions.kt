package fi.vm.yti.taxgen.dpmmodel

data class ProcessingOptions(
    val sqliteDbMandatoryLabelTranslationLanguage: Language?,
    val sqliteDbMandatoryLabelTranslationSourceCandidateLanguages: List<Language>?,
    val sqliteDbDpmElementUriStorageLabelTranslationLanguage: Language?
) {
    companion object {
        fun empty() = ProcessingOptions(null, null, null)
    }
}
