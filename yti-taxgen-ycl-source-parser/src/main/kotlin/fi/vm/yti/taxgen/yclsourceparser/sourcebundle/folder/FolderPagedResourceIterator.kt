package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.SortOps
import java.nio.file.Path

class FolderPagedResourceIterator(
    path: Path,
    filenameGlob: String
) : AbstractIterator<String>() {

    private val filePaths = resolveFilePaths(path, filenameGlob).iterator()

    override fun computeNext() {
        if (filePaths.hasNext()) {
            val fileContent = FileOps.readTextFile(filePaths.next())
            setNext(fileContent)
        } else {
            done()
        }
    }

    private fun resolveFilePaths(
        path: Path,
        filenameGlob: String
    ): List<Path> {
        val paths = FileOps.listFilesMatching(path, filenameGlob)
        return SortOps.folderContentSortedByNumberAwareFilename(paths)
    }
}
