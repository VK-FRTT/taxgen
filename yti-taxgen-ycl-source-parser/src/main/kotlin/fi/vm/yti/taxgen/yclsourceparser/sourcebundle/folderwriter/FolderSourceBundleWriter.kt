package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundleWriter
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path


class FolderSourceBundleWriter(
    private val rootPath: Path,
    private val sourceBundle: SourceBundle
) : SourceBundleWriter {

    override fun write() {
        val pathStack = PathStack(
            rootPath = rootPath,
            createFileSystemPaths = true
        )

        writeFile(pathStack, "bundleInfo.json", sourceBundle.bundleInfo().toJsonString())
        writeTaxonomyUnits(pathStack, sourceBundle.taxonomyUnits())
    }

    private fun writeTaxonomyUnits(
        pathStack: PathStack,
        taxonomyUnits: Iterator<TaxonomyUnit>
    ) {
        taxonomyUnits.withIndex().forEach { (unitIndex, unit) ->
            pathStack.pushSubfolderWithIndex("taxonomyunit", unitIndex)

            writeFile(pathStack, "owner.json", unit.owner().toJsonString())
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

            writeFile(pathStack, "codelist.json", list.codeListData())
            writeFile(pathStack, "codes.json", list.codesData())

            pathStack.pop()
        }
    }

    private fun writeFile(pathStack: PathStack, filename: String, content: String) {
        Files.newBufferedWriter(
            pathStack.resolvePath(filename),
            Charset.forName("UTF-8")
        ).use { writer ->
            writer.write(content)
        }
    }

    override fun close() {
    }
}
