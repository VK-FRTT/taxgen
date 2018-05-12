package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import java.nio.file.Path

class FolderSourceBundle(
    baseFolderPath: Path
) : SourceBundle {

    private val baseFolderPath = baseFolderPath.toAbsolutePath().normalize()

    override fun bundleDescriptor(): String {
        return FileOps.readTextFile(baseFolderPath, "bundle.json")
    }

    override fun taxonomyUnits(): List<TaxonomyUnit> {
        val taxonomyUnitPaths = FileOps.listSubFoldersMatching(baseFolderPath, "taxonomyunit_*")
        return taxonomyUnitPaths.map { FolderTaxonomyUnit(it) }
    }

    override fun close() {}
}
