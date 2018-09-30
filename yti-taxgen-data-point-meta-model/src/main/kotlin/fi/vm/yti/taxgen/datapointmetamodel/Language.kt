package fi.vm.yti.taxgen.datapointmetamodel

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.throwFail
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateTranslatedText
import java.net.URL
import java.nio.file.Path

class Language constructor(
    val iso6391Code: String,
    val label: TranslatedText
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) { //TODO  - is this really called?
        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Language::iso6391Code,
            minLength = 2,
            maxLength = 2
        )

        validateTranslatedText(
            validationErrors = validationErrors,
            instance = this,
            property = Language::label,
            minTranslationLength = 5,
            minLangCount = 1
        )
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

        data class LanguageConfig(

            val iso6391Code: String,
            val label: Map<String, String>
        )

        private val languages = loadLanguages()

        fun languages() = languages

        fun findByIso6391Code(iso6391Code: String): Language? {
            return languages.find { it.iso6391Code == iso6391Code }
        }

        internal fun loadLanguages(languageConfigPath: Path? = null): Set<Language> {
            val configUrl = resolveConfigUrl(languageConfigPath)

            val configs = loadLanguageConfigs(configUrl)

            val languages = initLanguages(configs)

            val defaultLabelLanguage = selectDefaultLabelLanguage(languages.keys)

            configureLanguageLabels(languages, defaultLabelLanguage)

            return languages.keys
        }

        private fun resolveConfigUrl(languageConfigPath: Path?): URL =
            if (languageConfigPath == null) {
                val contextClassLoader = Thread.currentThread().contextClassLoader
                contextClassLoader.getResource("languages/languages.json")
            } else {
                languageConfigPath.toUri().toURL()
            }

        private fun loadLanguageConfigs(configUrl: URL): List<LanguageConfig> {
            try {
                return JsonOps.lenientObjectMapper.readValue(configUrl)
            } catch (e: JsonProcessingException) {
                throwFail("Language configuration loading failed: ${e.message}")
            }
        }

        private fun initLanguages(configs: List<LanguageConfig>): Map<Language, LanguageConfig> =
            configs
                .sortedBy { it.iso6391Code }
                .map { Pair(initLanguage(it.iso6391Code), it) }
                .toMap()

        private fun configureLanguageLabels(
            languages: Map<Language, LanguageConfig>,
            defaultLabelLanguage: Language
        ) {
            val languageSet = languages.keys

            languages.forEach { (language, config) ->

                val translations = config.label.map { (langCode, text) ->
                    val translationLanguage = languageSet.find { it.iso6391Code == langCode }
                        ?: throwFail("Language configuration missing language '$langCode' used for label '$text'")

                    Pair(translationLanguage, text)
                }.toMap()

                (language.label.translations as MutableMap).putAll(translations)
                language.label.defaultLanguage = defaultLabelLanguage
            }
        }

        private fun initLanguage(iso6391Code: String) = Language(iso6391Code, TranslatedText(mutableMapOf()))

        private fun selectDefaultLabelLanguage(languages: Set<Language>): Language =
            languages.find { it.iso6391Code == "en" }
                ?: throwFail("Language configuration missing mandatory default language 'en'")
    }
}
