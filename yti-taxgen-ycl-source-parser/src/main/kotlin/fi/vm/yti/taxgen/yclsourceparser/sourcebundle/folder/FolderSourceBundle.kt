package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import java.nio.file.Path

class FolderSourceBundle(sourceBundleFolder: Path) :
    SourceBundle {

    override fun taxonomyUnits(): Iterator<TaxonomyUnit> {
        return FolderTaxonomyUnitIterator()
    }

    override fun close() {
    }
}
