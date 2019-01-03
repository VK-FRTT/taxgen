package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import java.nio.file.Path

class SourceProviderZipFileAdapter(
    private val sourceZipPath: Path
) : SourceProvider {

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        val dpmSource = DpmSourceZipFileAdapter(sourceZipPath)
        action(dpmSource)
        dpmSource.close()
    }
}
