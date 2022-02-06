package fi.vm.yti.taxgen.rdsource.folder

import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.helpers.SortOps
import java.nio.file.Path

internal class NumberedFilesIterator(
    path: Path,
    filenameGlob: String,
    private val diagnostic: Diagnostic
) : AbstractIterator<String>() {

    private val filePaths = resolveFilePaths(path, filenameGlob).iterator()

    override fun computeNext() {
        if (filePaths.hasNext()) {

            val path = filePaths.next()
            diagnostic.debug("Loading file: $path")

            val fileContent = FileOps.readTextFile(path)
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
