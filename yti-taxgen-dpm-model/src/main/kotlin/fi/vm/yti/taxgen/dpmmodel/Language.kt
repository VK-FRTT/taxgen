package fi.vm.yti.taxgen.dpmmodel

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationResultCollector
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropTranslatedText
import java.net.URL
import java.nio.file.Path

class Language constructor(
    val iso6391Code: String,
    val label: TranslatedText
) : Validatable {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::iso6391Code,
            minLength = 2,
            maxLength = 2
        )

        validatePropTranslatedText(
            validationResultBuilder = validationResultBuilder,
            property = this::label,
            minTranslationLength = 5,
            minLangCount = 1
        )
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "Language",
            subjectIdentifiers = listOf(iso6391Code)
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

        fun byIso6391CodeOrFail(iso6391Code: String): Language {
            return findByIso6391Code(iso6391Code)
                ?: throwIllegalDpmModelState("Language configuration missing requested language '$iso6391Code'")
        }

        internal fun loadLanguages(configPath: Path? = null): Set<Language> {
            val configUrl = resolveConfigUrl(configPath, "languages/languages.json")

            val configs = loadLanguageConfigs(configUrl)

            val languages = initLanguages(configs)

            configureLanguageLabels(languages)

            val plainLanguages = languages.keys

            validateLanguages(plainLanguages)

            return plainLanguages.sortedBy { it.iso6391Code }.toSet()
        }

        private fun resolveConfigUrl(languageConfigPath: Path?, fallbackResourceName: String): URL =
            if (languageConfigPath == null) {
                val contextClassLoader = Thread.currentThread().contextClassLoader
                contextClassLoader.getResource(fallbackResourceName)
            } else {
                languageConfigPath.toUri().toURL()
            }

        private fun loadLanguageConfigs(configUrl: URL): List<LanguageConfig> {
            try {
                return jacksonObjectMapper().readValue(configUrl)
            } catch (e: JsonProcessingException) {
                throwIllegalDpmModelState("Language configuration loading failed: ${e.message}")
            }
        }

        private fun jacksonObjectMapper(): ObjectMapper {
            val mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
            mapper.registerModule(JavaTimeModule())

            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

            return mapper
        }

        private fun initLanguages(configs: List<LanguageConfig>): Map<Language, LanguageConfig> {
            fun initLanguage(iso6391Code: String) = Language(iso6391Code, TranslatedText(mutableMapOf()))

            return configs
                .map { Pair(initLanguage(it.iso6391Code), it) }
                .toMap()
        }

        private fun configureLanguageLabels(
            languages: Map<Language, LanguageConfig>
        ) {
            val languageSet = languages.keys

            languages.forEach { (language, config) ->

                val translations = config.label.map { (langCode, text) ->
                    val translationLanguage = languageSet.find { it.iso6391Code == langCode }
                        ?: throwIllegalDpmModelState("Language configuration missing language '$langCode' used for label '$text'")

                    Pair(translationLanguage, text)
                }.toMap()

                (language.label.translations as MutableMap).putAll(translations)
            }
        }

        private fun validateLanguages(languages: Set<Language>) {

            val validationMessages = languages.mapIndexedNotNull { index, language ->
                val validationCollector = ValidationResultCollector()
                language.validate(validationCollector)

                val results = validationCollector.uniqueResults()

                if (results.any()) {
                    val subject = language.validationSubjectDescriptor()
                    "${subject.subjectType} #$index (${subject.subjectIdentifiers.first()}): ${results.joinToString { it.toString() }}"
                } else {
                    null
                }
            }

            validationMessages
                .takeIf { it.any() }
                ?.let { throwIllegalDpmModelState("Language configuration not valid. $it") }
        }
    }
}
