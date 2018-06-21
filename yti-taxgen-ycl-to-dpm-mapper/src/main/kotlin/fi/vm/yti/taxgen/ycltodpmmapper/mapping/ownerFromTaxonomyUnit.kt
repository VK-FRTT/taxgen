package fi.vm.yti.taxgen.ycltodpmmapper.mapping

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnitInfo

fun ownerFromTaxonomyUnit(unit: TaxonomyUnit): Owner {
    val mapper = JacksonObjectMapper.lenientObjectMapper()
    val taxonomyUnitInfo: TaxonomyUnitInfo = mapper.readValue(unit.taxonomyUnitInfoData())

    return Owner(
        name = taxonomyUnitInfo.name,
        namespace = taxonomyUnitInfo.namespace,
        prefix = taxonomyUnitInfo.prefix,
        location = taxonomyUnitInfo.location,
        copyright = taxonomyUnitInfo.copyright,
        supportedLanguages = taxonomyUnitInfo.supportedLanguages
    )
}
