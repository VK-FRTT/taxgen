package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import java.time.Instant
import java.time.LocalDate

abstract class RdsEntity {
    abstract val id: String?
    abstract val uri: String?
    abstract val codeValue: String?

    abstract val created: Instant?
    abstract val modified: Instant?
    abstract val startDate: LocalDate?
    abstract val endDate: LocalDate?

    abstract val prefLabel: Map<String, String>?
    abstract val description: Map<String, String>?

    fun diagnosticLabel(): String {
        return prefLabel?.entries?.firstOrNull { it.value.isNotBlank() }?.value ?: ""
    }

    fun diagnosticIdentifier(): String {
        return uri ?: ""
    }

    fun validUri(diagnostic: Diagnostic): String {
        return uri ?: diagnostic.fatal("RDS Entity not having valid URI")
    }

    fun hasUri(uri: String?): Boolean {
        if (this.uri == null) return false
        if (uri == null) return false

        return (this.uri == uri)
    }

    fun idOrEmpty(): String {
        return id ?: ""
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
