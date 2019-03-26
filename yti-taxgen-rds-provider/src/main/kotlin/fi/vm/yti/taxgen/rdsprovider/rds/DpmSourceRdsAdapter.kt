package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig

internal class DpmSourceRdsAdapter(
    private val dpmSourceConfig: DpmSourceConfig,
    private val diagnostic: Diagnostic
) : DpmSource {

    override fun contextLabel(): String = "Reference Data service"
    override fun contextIdentifier(): String = "config file: ${dpmSourceConfig.configFilePath}"
    override fun sourceConfigData(): String = dpmSourceConfig.rawConfigData

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        dpmSourceConfig.dpmDictionaries.forEach { config ->
            val dictionarySource = DpmDictionarySourceRdsAdapter(
                config,
                diagnostic
            )

            action(dictionarySource)
        }
    }
}
