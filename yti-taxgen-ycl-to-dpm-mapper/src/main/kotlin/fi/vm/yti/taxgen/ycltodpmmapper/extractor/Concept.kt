package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCode
import fi.vm.yti.taxgen.ycltodpmmapper.yclmodel.YclCodeScheme
import java.time.Instant

internal fun Concept.Companion.fromYclCodeScheme(
    codeScheme: YclCodeScheme,
    owner: Owner
): Concept {
    return Concept(
        createdAt = codeScheme.created ?: Instant.EPOCH,
        modifiedAt = codeScheme.modified ?: Instant.EPOCH,
        applicableFrom = codeScheme.startDate,
        applicableUntil = codeScheme.endDate,
        label = TranslatedText.fromYclLangText(codeScheme.prefLabel, owner.languages),
        description = TranslatedText.fromYclLangText(codeScheme.description, owner.languages),
        owner = owner
    )
}

internal fun Concept.Companion.fromYclCode(
    code: YclCode,
    owner: Owner
): Concept {
    return Concept(
        createdAt = code.created ?: Instant.EPOCH,
        modifiedAt = code.modified ?: Instant.EPOCH,
        applicableFrom = code.startDate,
        applicableUntil = code.endDate,
        label = TranslatedText.fromYclLangText(code.prefLabel, owner.languages),
        description = TranslatedText.fromYclLangText(code.description, owner.languages),
        owner = owner
    )
}
