package fi.vm.yti.taxgen.yclsourceparser.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnitDescriptor
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper

fun ownerFromTaxonomyUnit(unit: TaxonomyUnit): Owner {
    val mapper = JacksonObjectMapper.lenientObjectMapper()
    val taxonomyUnitDescriptor: TaxonomyUnitDescriptor = mapper.readValue(unit.taxonomyUnitDescriptor())

    return Owner(
        namespace = taxonomyUnitDescriptor.namespace,
        namespacePrefix = taxonomyUnitDescriptor.namespacePrefix,
        officialLocation = taxonomyUnitDescriptor.officialLocation,
        copyrightText = taxonomyUnitDescriptor.copyrightText,
        supportedLanguages = taxonomyUnitDescriptor.supportedLanguages
    )
}
