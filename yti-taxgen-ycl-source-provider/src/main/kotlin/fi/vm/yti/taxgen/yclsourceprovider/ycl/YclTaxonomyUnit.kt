package fi.vm.yti.taxgen.yclsourceprovider.ycl

import fi.vm.yti.taxgen.commons.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceprovider.CodeList
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnitInfo
import fi.vm.yti.taxgen.yclsourceprovider.ycl.config.TaxonomyUnitConfig

class YclTaxonomyUnit(
    private val taxonomyUnitConfig: TaxonomyUnitConfig
) : TaxonomyUnit {

    override fun taxonomyUnitInfoData(): String {
        val info = TaxonomyUnitInfo(
            name = taxonomyUnitConfig.name,
            namespace = taxonomyUnitConfig.namespace,
            prefix = taxonomyUnitConfig.prefix,
            location = taxonomyUnitConfig.location,
            copyright = taxonomyUnitConfig.copyright,
            supportedLanguages = taxonomyUnitConfig.supportedLanguages
        )

        return info.toJsonString()
    }

    override fun codeLists(): List<CodeList> {
        return taxonomyUnitConfig.codeLists.map { YclCodeList(it) }
    }
}
