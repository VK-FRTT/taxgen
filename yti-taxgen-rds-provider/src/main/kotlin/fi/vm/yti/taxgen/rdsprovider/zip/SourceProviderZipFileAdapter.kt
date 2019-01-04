package fi.vm.yti.taxgen.rdsprovider.zip

import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.SourceProvider
import java.nio.file.Path

class SourceProviderZipFileAdapter(
    private val sourceZipPath: Path
) : SourceProvider {

    private val dpmSource: DpmSourceZipFileAdapter by lazy {
        DpmSourceZipFileAdapter(sourceZipPath)
    }

    override fun withDpmSource(action: (DpmSource) -> Unit) {
        action(dpmSource)
    }

    override fun close() {
        dpmSource.close()
    }
}
