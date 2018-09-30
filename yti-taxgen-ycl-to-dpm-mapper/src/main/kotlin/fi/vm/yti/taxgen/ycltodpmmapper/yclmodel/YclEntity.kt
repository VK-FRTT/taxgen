package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.ycltodpmmapper.extractor.fromYclLangText
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

    fun composeContextName(): String {
        return prefLabel?.get("en") ?: "" //TODO
    }

    fun composeContextRef(): String {
        return "$uri"
        //TODO handle nulls,
        //TODO extend URI with env=foo param
    }

    fun identityOrEmpty(): String {
        return id ?: ""
    }

    fun codeValueOrEmpty(): String {
        return codeValue ?: ""
    }

    fun diagnosticLabel(): String {
        val contextName = composeContextName()
        if (contextName.isNotEmpty()) return contextName

        return composeContextRef()
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
