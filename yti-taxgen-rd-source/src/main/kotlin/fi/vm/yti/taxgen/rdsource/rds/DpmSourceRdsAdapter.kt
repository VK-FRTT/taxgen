package fi.vm.yti.taxgen.rdsource.rds

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmDictionarySource
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.DpmSourceConfigHolder

internal class DpmSourceRdsAdapter(
    private val configHolder: DpmSourceConfigHolder,
    private val diagnosticContext: DiagnosticContext
) : DpmSource {

    private val rdsClient = RdsClient(diagnosticContext)

    override fun contextTitle(): String = "Reference Data service"
    override fun contextIdentifier(): String = "config file: ${configHolder.configFilePath}"
    override fun config(): DpmSourceConfigHolder = configHolder

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        configHolder.dpmSourceConfig.dpmDictionaryConfigs.forEach { dictionaryConfig ->
            val dictionarySource = DpmDictionarySourceRdsAdapter(
                dictionaryConfig,
                rdsClient,
                diagnosticContext
            )

            action(dictionarySource)
        }
    }
}
