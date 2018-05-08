package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import java.nio.file.Path

class FolderSourceBundleWriter(
    private val rootPath: Path,
    private val sourceBundle: SourceBundle
) : SourceBundleWriter {

    override fun write() {
        val pathStack = PathStack(rootPath)

        //writeFile(pathStack, "meta.json", sourceBundle.metaData().asJson())
        writeTaxonomyUnits(pathStack, sourceBundle.taxonomyUnits())
    }

    private fun writeTaxonomyUnits(
        pathStack: PathStack,
        taxonomyUnits: Iterator<TaxonomyUnit>
    ) {
        taxonomyUnits.withIndex().forEach { (unitIndex, unit) ->
            pathStack.pushSubfolderWithIndex("taxonomyunit", unitIndex)
            ensurePathExists(pathStack)

            //writeFile(pathStack, "owner.json", unit.owner().asJson())
            writeCodeLists(pathStack, unit.codeLists())

            pathStack.pop()
        }
    }

    private fun writeCodeLists(
        pathStack: PathStack,
        codeLists: Iterator<CodeList>
    ) {
        codeLists.withIndex().forEach { (listIndex, list) ->
            pathStack.pushSubfolderWithIndex("codelist", listIndex)
            ensurePathExists(pathStack)

            writeFile(pathStack, "codelist.json", list.codeListData())
            writeFile(pathStack, "codes.json", list.codesData())

            pathStack.pop()
        }
    }

    private fun ensurePathExists(pathStack: PathStack) {
        pathStack.currentPath().toFile().mkdirs()
    }

    private fun writeFile(pathStack: PathStack, filename: String, content: String) {
        pathStack.resolvePath(filename).toFile().writeText(content)
    }

    override fun close() {
    }
}
