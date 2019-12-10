package fi.vm.yti.taxgen.rdsource.rds

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsource.DpmSource
import fi.vm.yti.taxgen.rdsource.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsource.SourceHolder

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
