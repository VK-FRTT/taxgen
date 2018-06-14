package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers

import java.nio.file.Path

object SortOps {

    fun folderContentSortedByNumberAwareFilename(paths: List<Path>): List<Path> {
        ensureSingleParentPath(paths)
        return paths.sortedWith(NumberAwareFilenameComparator.instance())
    }

    private fun ensureSingleParentPath(paths: List<Path>) {
        if (paths.size <= 1) return
        val firstParent = paths[0].parent.toString()
        check(paths.all { it.parent.toString() == firstParent })
    }
}
