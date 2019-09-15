package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfigHolder

internal class DpmSourceRdsAdapter(
    private val configHolder: DpmSourceConfigHolder,
    private val diagnostic: Diagnostic
) : DpmSource {

    private val rdsClient = RdsClient(diagnostic)

    override fun contextLabel(): String = "Reference Data service"
    override fun contextIdentifier(): String = "config file: ${configHolder.configFilePath}"
    override fun config(): DpmSourceConfigHolder = configHolder

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        configHolder.dpmSourceConfig.dpmDictionaryConfigs.forEach { dictionaryConfig ->
            val dictionarySource = DpmDictionarySourceRdsAdapter(
                dictionaryConfig,
                rdsClient,
                diagnostic
            )

            action(dictionarySource)
        }
    }
}
