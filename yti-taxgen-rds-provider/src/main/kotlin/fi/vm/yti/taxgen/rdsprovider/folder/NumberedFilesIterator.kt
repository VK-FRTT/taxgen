package fi.vm.yti.taxgen.rdsprovider.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.rdsprovider.helpers.SortOps
import java.nio.file.Path

internal class NumberedFilesIterator(
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
