package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource

class DpmDictionarySourceApiAdapter(
    private val config: DpmDictionarySourceApiAdapterConfig
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return config.ownerInfoData()
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        return config.codelistConfigs.map { YclCodelistSourceApiAdapter(it) }
    }
}
