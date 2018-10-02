package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.config.DpmDictionarySourceConfig

internal class DpmDictionarySourceApiAdapter(
    private val config: DpmDictionarySourceConfig,
    private val diagnostic: Diagnostic
) : DpmDictionarySource {

    override fun dpmOwnerConfigData(): String {
        return JsonOps.writeAsJsonString(config.owner)
    }

    override fun yclCodelistSources(): List<YclCodelistSource> {
        return config.codelists.map { config ->
            YclCodelistSourceApiAdapter(
                config,
                diagnostic
            )
        }
    }
}
