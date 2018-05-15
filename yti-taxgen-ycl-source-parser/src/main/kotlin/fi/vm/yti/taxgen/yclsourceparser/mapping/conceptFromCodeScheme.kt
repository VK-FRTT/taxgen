package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.yclsourceparser.model.CodeScheme

fun conceptFromCodeScheme(codeScheme: CodeScheme): Concept {
    return Concept(
        createdAt = codeScheme.modified,
        modifiedAt = codeScheme.modified,
        applicableFrom = codeScheme.startDate,
        applicableUntil = codeScheme.endDate,
        label = TranslatedText(codeScheme.prefLabel!!),
        description = TranslatedText(codeScheme.description!!)
    )
}
