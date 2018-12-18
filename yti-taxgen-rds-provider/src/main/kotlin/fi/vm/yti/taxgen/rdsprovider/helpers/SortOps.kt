package fi.vm.yti.taxgen.rdsprovider.helpers

import java.nio.file.Path

object SortOps {

    fun folderContentSortedByNumberAwareFilename(paths: List<Path>): List<Path> {
        ensurePathsWithinSingleParent(paths)
        return paths.sortedWith(NumberAwareFilenameComparator.instance())
    }

    private fun ensurePathsWithinSingleParent(paths: List<Path>) {
        if (paths.size <= 1) return
        val firstParent = paths[0].parent.toString()
        check(paths.all { it.parent.toString() == firstParent })
    }
}
