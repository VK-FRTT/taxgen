package fi.vm.yti.taxgen.yclsourceparser.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnitInfo
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper

fun ownerFromTaxonomyUnit(unit: TaxonomyUnit): Owner {
    val mapper = JacksonObjectMapper.lenientObjectMapper()
    val taxonomyUnitInfo: TaxonomyUnitInfo = mapper.readValue(unit.taxonomyUnitInfoData())

    return Owner(
        namespace = taxonomyUnitInfo.namespace,
        namespacePrefix = taxonomyUnitInfo.namespacePrefix,
        officialLocation = taxonomyUnitInfo.officialLocation,
        copyrightText = taxonomyUnitInfo.copyrightText,
        supportedLanguages = taxonomyUnitInfo.supportedLanguages
    )
}
