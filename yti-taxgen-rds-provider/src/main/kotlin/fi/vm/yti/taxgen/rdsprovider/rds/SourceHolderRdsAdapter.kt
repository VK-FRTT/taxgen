package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsprovider.SourceHolder

class SourceHolderRdsAdapter(
    private val configHolder: DpmSourceConfigHolder,
    private val diagnosticContext: DiagnosticContext
) : SourceHolder {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceRdsAdapter(configHolder, diagnosticContext)
        action(dpmSource)
    }

    override fun close() {}
}
