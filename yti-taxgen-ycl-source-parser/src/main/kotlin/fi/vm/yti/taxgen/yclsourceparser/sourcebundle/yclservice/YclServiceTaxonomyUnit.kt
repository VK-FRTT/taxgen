package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnitInfo
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.TaxonomyUnitConfig

class YclServiceTaxonomyUnit(
    private val taxonomyUnitConfig: TaxonomyUnitConfig
) : TaxonomyUnit {

    override fun taxonomyUnitInfoData(): String {
        val info = TaxonomyUnitInfo(
            namespace = taxonomyUnitConfig.namespace,
            namespacePrefix = taxonomyUnitConfig.namespacePrefix,
            officialLocation = taxonomyUnitConfig.officialLocation,
            copyrightText = taxonomyUnitConfig.copyrightText,
            supportedLanguages = taxonomyUnitConfig.supportedLanguages
        )

        return info.toJsonString()
    }

    override fun codeLists(): List<CodeList> {
        return taxonomyUnitConfig.codeLists.map { YclServiceCodeList(it) }
    }
}
