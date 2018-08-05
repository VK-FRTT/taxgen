package fi.vm.yti.taxgen.datapointmetamodel

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

data class Language private constructor(
    val iso6391Code: String,
    val label: TranslatedText
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {
        validateProperty(
            instance = this,
            property = "iso6391Code",
            minLength = 2,
            maxLength = 2
        )

        //TODO - validate label text not blank
    }

    override fun hashCode(): Int {
        return iso6391Code.hashCode()
    }

    override fun toString(): String {
        val labelString = label.translations.map { (lang, text) -> "${lang.iso6391Code}: $text" }.joinToString()
        return "{Language(iso6391Code=$iso6391Code, label={$labelString})"
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other !is Language) {
            return false
        }

        return other.iso6391Code == iso6391Code
    }

    companion object {

        private val allLanguages = resolveAllLanguages()

        fun allLanguages() = allLanguages

        fun findByIso6391Code(iso6391Code: String): Language? {
            return allLanguages.find { it.iso6391Code == iso6391Code }
        }

        private fun resolveAllLanguages(): Set<Language> {
            val configs = loadConfigs()

            val languages = initLanguages(configs)

            val defaultLabelLanguage = selectDefaultLabelLanguage(languages.keys)

            configureLanguageLabels(languages, defaultLabelLanguage)

            return languages.keys
        }

        private fun loadConfigs(): List<LanguageConfig> {
            val contextClassLoader = Thread.currentThread().contextClassLoader
            val langConfigUrl = contextClassLoader.getResource("languages/languages.json")
            return JacksonObjectMapper.lenientObjectMapper().readValue(langConfigUrl)
        }

        private fun initLanguages(configs: List<LanguageConfig>): Map<Language, LanguageConfig> {
            val languages = configs
                .sortedBy { it.iso6391Code }
                .map { Pair(initLanguage(it.iso6391Code), it) }
                .toMap()

            return languages
        }

        private fun configureLanguageLabels(
            languages: Map<Language, LanguageConfig>,
            defaultLabelLanguage: Language
        ) {
            val languageSet = languages.keys

            languages.forEach { (language, config) ->
                val translations = resolveTranslations(config, languageSet)

                (language.label.translations as MutableMap).putAll(translations)
                language.label.defaultLanguage = defaultLabelLanguage
            }
        }

        private fun resolveTranslations(
            config: LanguageConfig,
            languages: Set<Language>
        ): Map<Language, String> {
            //TODO - handle config error of missing target language

            val translations = config.label
                .map { (code, text) ->
                    val translationLanguage = languages.find { it.iso6391Code == code }!!

                    Pair(translationLanguage, text)
                }.toMap()

            return translations
        }

        private fun initLanguage(iso6391Code: String) = Language(iso6391Code, TranslatedText(mutableMapOf()))

        private fun selectDefaultLabelLanguage(languages: Set<Language>): Language {
            return languages.find { it.iso6391Code == "en" }!! //TODO - handle config error of missing language
        }
    }
}
