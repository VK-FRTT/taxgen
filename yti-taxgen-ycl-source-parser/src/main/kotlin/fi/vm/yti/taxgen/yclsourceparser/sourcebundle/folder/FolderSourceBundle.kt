package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.BundleInfo
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import java.nio.file.Path
import java.time.Instant

class FolderSourceBundle(sourceBundleFolder: Path) :
    SourceBundle {

    private val sourceBundleInfo = BundleInfo(
        sourceBundleType = "FolderSourceBundle",
        createdAt = Instant.now().toString()
    )

    override fun bundleInfo(): BundleInfo = sourceBundleInfo

    override fun taxonomyUnits(): Iterator<TaxonomyUnit> {
        return FolderTaxonomyUnitIterator()
    }

    override fun close() {
    }
}
