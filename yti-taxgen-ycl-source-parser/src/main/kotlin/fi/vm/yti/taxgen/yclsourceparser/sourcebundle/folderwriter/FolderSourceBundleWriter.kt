package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import java.io.BufferedWriter
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class FolderSourceBundleWriter(
    private val folderPath: Path,
    private val sourceBundle: SourceBundle,
    private val forceOverwrite: Boolean
) : SourceBundleWriter {

    override fun write() {
        val pathStack = createPathStack()

        writeFile(pathStack, "bundleInfo.json", sourceBundle.bundleInfo().toJsonString())
        writeTaxonomyUnits(pathStack, sourceBundle.taxonomyUnits())
    }

    private fun createPathStack(): PathStack {
        return PathStack(
            rootPath = folderPath,
            createFileSystemPaths = true
        )
    }

    private fun writeTaxonomyUnits(
        pathStack: PathStack,
        taxonomyUnits: Iterator<TaxonomyUnit>
    ) {
        taxonomyUnits.withIndex().forEach { (unitIndex, unit) ->

            pathStack.withIndexedSubfolder("taxonomyunit", unitIndex) {

                writeFile(pathStack, "owner.json", unit.owner().toJsonString())
                writeCodeLists(pathStack, unit.codeLists())
            }
        }
    }

    private fun writeCodeLists(
        pathStack: PathStack,
        codeLists: Iterator<CodeList>
    ) {
        codeLists.withIndex().forEach { (listIndex, list) ->

            pathStack.withIndexedSubfolder("codelist", listIndex) {

                writeFile(pathStack, "codelist.json", list.codeListData())
                writeFile(pathStack, "codes.json", list.codesData())
            }
        }
    }

    private fun writeFile(pathStack: PathStack, filename: String, content: String) {
        val writerResource = createBufferedWriter(pathStack, filename)

        writerResource.use {
            it.write(content)
        }
    }

    private fun createBufferedWriter(pathStack: PathStack, filename: String): BufferedWriter {
        val path = pathStack.resolvePath(filename)
        val charset = Charset.forName("UTF-8")

        return if (forceOverwrite) {
            Files.newBufferedWriter(path, charset)
        } else {
            Files.newBufferedWriter(path, charset, StandardOpenOption.CREATE_NEW)
        }
    }

    override fun close() {
    }
}
