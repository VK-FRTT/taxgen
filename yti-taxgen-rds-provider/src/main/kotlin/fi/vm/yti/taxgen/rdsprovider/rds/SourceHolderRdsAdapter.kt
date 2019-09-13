package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceHolder
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig

class SourceHolderRdsAdapter(
    private val dpmSourceConfig: DpmSourceConfig,
    private val diagnostic: Diagnostic
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceRdsAdapter(dpmSourceConfig, diagnostic)
        action(dpmSource)
    }

    override fun close() {}
}
