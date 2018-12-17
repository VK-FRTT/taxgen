package fi.vm.yti.taxgen.rdsdpmmapper.yclmodel

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.rdsdpmmapper.extractor.fromYclLangText
import java.time.Instant
import java.time.LocalDate

abstract class YclEntity {
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

    fun idOrEmpty(): String {
        return id ?: ""
    }

    fun uriOrEmpty(): String {
        return uri ?: ""
    }

    fun codeValueOrEmpty(): String {
        return codeValue ?: ""
    }

    open fun dpmConcept(owner: Owner): Concept {
        return Concept(
            createdAt = created ?: Instant.EPOCH,
            modifiedAt = modified ?: Instant.EPOCH,
            applicableFrom = startDate,
            applicableUntil = endDate,
            label = TranslatedText.fromYclLangText(prefLabel, owner.languages),
            description = TranslatedText.fromYclLangText(description, owner.languages),
            owner = owner
        )
    }
}
