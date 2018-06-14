package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.yclsourceparser.model.YclCodeScheme

fun conceptFromYclCodeScheme(yclCodeScheme: YclCodeScheme): Concept {
    return Concept(
        createdAt = yclCodeScheme.modified,
        modifiedAt = yclCodeScheme.modified,
        applicableFrom = yclCodeScheme.startDate,
        applicableUntil = yclCodeScheme.endDate,
        label = TranslatedText(yclCodeScheme.prefLabel!!),
        description = TranslatedText(yclCodeScheme.description!!)
    )
}
