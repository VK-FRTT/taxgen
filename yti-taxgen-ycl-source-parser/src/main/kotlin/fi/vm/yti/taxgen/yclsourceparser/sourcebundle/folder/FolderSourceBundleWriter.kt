package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.PathStack
import java.nio.file.Path

class FolderSourceBundleWriter(
    private val baseFolderPath: Path,
    private val sourceBundle: SourceBundle,
    private val forceOverwrite: Boolean
) : SourceBundleWriter {

    override fun write() {
        val pathStack = createPathStack()

        FileOps.writeTextFile(sourceBundle.bundleDescriptor(), pathStack, "bundle.json", forceOverwrite)
        writeTaxonomyUnits(sourceBundle.taxonomyUnits(), pathStack)
    }

    private fun createPathStack(): PathStack {
        return PathStack(
            baseFolderPath = baseFolderPath,
            createFileSystemPaths = true
        )
    }

    private fun writeTaxonomyUnits(taxonomyUnits: List<TaxonomyUnit>, pathStack: PathStack) {
        taxonomyUnits.withIndex().forEach { (unitIndex, unit) ->

            pathStack.withIndexPostfixSubfolder("taxonomyunit", unitIndex) {

                FileOps.writeTextFile(unit.taxonomyUnitDescriptor(), pathStack, "taxonomyunit.json", forceOverwrite)
                writeCodeLists(unit.codeLists(), pathStack)
            }
        }
    }

    private fun writeCodeLists(codeLists: List<CodeList>, pathStack: PathStack) {
        codeLists.withIndex().forEach { (listIndex, list) ->

            pathStack.withIndexPostfixSubfolder("codelist", listIndex) {

                FileOps.writeTextFile(list.codeList(), pathStack, "codelist.json", forceOverwrite)
                FileOps.writeTextFile(list.codes(), pathStack, "codes.json", forceOverwrite)
            }
        }
    }

    override fun close() {}
}
