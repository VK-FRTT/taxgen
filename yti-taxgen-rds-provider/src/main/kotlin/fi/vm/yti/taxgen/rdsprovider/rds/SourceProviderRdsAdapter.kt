package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import java.nio.file.Path

class SourceProviderRdsAdapter(
    private val configPath: Path,
    private val diagnostic: Diagnostic
) : SourceProvider {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceRdsAdapter(configPath, diagnostic)
        action(dpmSource)
    }

    override fun close() {}
}
