package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnitDescriptor
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclTaxonomyUnitConfig
import okhttp3.OkHttpClient

class YclServiceTaxonomyUnit(
    private val taxonomyUnitConfig: YclTaxonomyUnitConfig,
    private val httpClient: OkHttpClient
) : TaxonomyUnit {

    override fun taxonomyUnitDescriptor(): String {
        val descriptor = TaxonomyUnitDescriptor(
            namespace = taxonomyUnitConfig.namespace,
            namespacePrefix = taxonomyUnitConfig.namespacePrefix,
            officialLocation = taxonomyUnitConfig.officialLocation,
            copyrightText = taxonomyUnitConfig.copyrightText,
            supportedLanguages = taxonomyUnitConfig.supportedLanguages
        )

        return descriptor.toJsonString()
    }

    override fun codeLists(): List<CodeList> {
        return taxonomyUnitConfig.codeLists.map { YclServiceCodeList(it, httpClient) }
    }
}
