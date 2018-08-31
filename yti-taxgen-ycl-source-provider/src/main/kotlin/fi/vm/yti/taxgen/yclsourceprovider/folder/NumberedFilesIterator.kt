package fi.vm.yti.taxgen.yclsourceprovider.folder

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.helpers.SortOps
import java.nio.file.Path

class NumberedFilesIterator(
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
