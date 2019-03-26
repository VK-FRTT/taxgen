package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig

class SourceProviderRdsAdapter(
    private val dpmSourceConfig: DpmSourceConfig,
    private val diagnostic: Diagnostic
) : SourceProvider {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceRdsAdapter(dpmSourceConfig, diagnostic)
        action(dpmSource)
    }

    override fun close() {}
}
