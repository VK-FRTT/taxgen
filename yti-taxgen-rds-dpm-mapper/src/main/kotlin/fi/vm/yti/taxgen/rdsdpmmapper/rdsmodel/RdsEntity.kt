package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import java.time.Instant
import java.time.LocalDate

abstract class RdsEntity {
    val uri: String? = null
    val codeValue: String? = null

    val created: Instant? = null
    val modified: Instant? = null
    val startDate: LocalDate? = null
    val endDate: LocalDate? = null

    val prefLabel: Map<String, String>? = null
    val description: Map<String, String>? = null

    val order: Int? = null

    fun diagnosticContextTitleFromLabel(
        diagnosticSourceLanguages: List<Language>
    ): String {
        prefLabel ?: return ""

        return diagnosticSourceLanguages
            .map { prefLabel[it.iso6391Code] }
            .filterNotNull()
            .firstOrNull()
            ?: ""
    }

    fun validUri(diagnostic: Diagnostic): String {
        return uri ?: diagnostic.fatal("RDS Entity not having valid URI value")
    }

    fun validOrder(diagnostic: Diagnostic): Int {
        return order ?: diagnostic.fatal("RDS Entity not having valid Order value")
    }

    fun hasUri(uri: String?): Boolean {
        if (this.uri == null) return false
        if (uri == null) return false

        return (this.uri == uri)
    }

    fun codeValueOrEmpty(): String {
        return codeValue ?: ""
    }

    fun dpmConcept(owner: Owner): Concept {
        return Concept(
            createdAt = created ?: Instant.EPOCH,
            modifiedAt = modified ?: Instant.EPOCH,
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = toTranslatedText(prefLabel, owner.languages),
            description = toTranslatedText(description, owner.languages),
            owner = owner
        )
    }

    private fun toTranslatedText(
        langText: Map<String, String>?,
        languageSet: Set<Language>
    ): TranslatedText {

        fun resolveTranslations(): Map<Language, String> {
            if (langText == null) {
                return emptyMap()
            }

            return langText
                .map { (langCode, text) ->
                    val language = languageSet.find { it.iso6391Code == langCode }

                    if (language == null) {
                        null
                    } else {
                        Pair(language, text)
                    }
                }
                .filterNotNull()
                .toMap()
        }

        return TranslatedText(resolveTranslations())
    }
}
