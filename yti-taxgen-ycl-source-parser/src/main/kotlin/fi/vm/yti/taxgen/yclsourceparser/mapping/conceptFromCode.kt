package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.yclsourceparser.model.Code

fun conceptFromCode(code: Code): Concept {
    return Concept(
        createdAt = code.modified,
        modifiedAt = code.modified,
        applicableFrom = code.startDate,
        applicableUntil = code.endDate,
        label = TranslatedText(code.prefLabel!!),
        description = TranslatedText(code.description!!)
    )
}
