package fi.vm.yti.taxgen.datapointmetamodel

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.languages.LanguageConfig

object Languages {

    private val languages = resolveLanguages()

    private fun resolveLanguages(): List<Language> {
        val languageConfigs = loadLanguageConfigs()

        val languages = initLanguages(languageConfigs)

        populateLabels(languageConfigs, languages)

        return languages
    }

    private fun loadLanguageConfigs(): List<LanguageConfig> {
        val contextClassLoader = Thread.currentThread().contextClassLoader
        val langConfigUrl = contextClassLoader.getResource("languages/languages.json")
        return JacksonObjectMapper.lenientObjectMapper().readValue(langConfigUrl)
    }

    private fun initLanguages(languageConfig: List<LanguageConfig>): List<Language> {
        return languageConfig
            .sortedBy {
                it.iso6391Code
            }
            .map {
                Language(
                    iso6391Code = it.iso6391Code,
                    label = mutableMapOf()
                )
            }
    }

    private fun populateLabels(
        languageConfigs: List<LanguageConfig>,
        languages: List<Language>
    ) {
        languages.forEach { language ->
            val languageConfig = languageConfigs.find { config -> config.iso6391Code == language.iso6391Code }!!

            val populatedLabel = populateLabel(languageConfig, languages)

            val mutableLabel = (language.label as MutableMap)
            mutableLabel.putAll(populatedLabel)
        }
    }

    private fun populateLabel(
        languageConfig: LanguageConfig,
        languages: List<Language>
    ): List<Pair<Language, String>> {
        return languageConfig.label.map { (configCode, configText) ->
            val languageForCode =
                languages.find { language -> configCode == language.iso6391Code }!! //TODO - handle config error of missing target language
            Pair(
                languageForCode,
                configText
            )
        }
    }

    fun languages() = languages
}
