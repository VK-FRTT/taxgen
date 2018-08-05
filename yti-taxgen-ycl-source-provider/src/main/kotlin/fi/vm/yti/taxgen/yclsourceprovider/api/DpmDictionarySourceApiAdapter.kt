package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource

class DpmDictionarySourceApiAdapter(
    index: Int,
    private val config: DpmDictionarySourceApiAdapterConfig
) : DpmDictionarySource(index) {

    override fun dpmOwnerConfigData(): String {
        return config.ownerInfoData()
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        return config.codelistConfigs.mapIndexed { index, config -> YclCodelistSourceApiAdapter(index, config) }
    }
}
