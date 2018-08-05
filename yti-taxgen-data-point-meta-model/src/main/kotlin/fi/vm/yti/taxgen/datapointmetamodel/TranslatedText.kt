package fi.vm.yti.taxgen.datapointmetamodel

data class TranslatedText(
    val translations: Map<Language, String>
) {
    companion object {} //ktlint-disable no-empty-class-body

    internal lateinit var defaultLanguage: Language

    fun defaultText(): String? {
        return translations[defaultLanguage]
    }
}
