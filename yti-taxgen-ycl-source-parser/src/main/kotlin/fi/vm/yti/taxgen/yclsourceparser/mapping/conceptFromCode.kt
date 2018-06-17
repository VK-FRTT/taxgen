package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.yclsourceparser.model.YclCode

fun conceptFromYclCode(yclCode: YclCode): Concept {
    return Concept(
        createdAt = yclCode.created!!, //TODO - report errors if mandatory fields are missing in YCL data
        modifiedAt = yclCode.modified!!,
        applicableFrom = yclCode.startDate,
        applicableUntil = yclCode.endDate,
        label = TranslatedText(yclCode.prefLabel!!),
        description = TranslatedText(yclCode.description!!)
    )
}
