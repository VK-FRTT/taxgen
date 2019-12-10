package fi.vm.yti.taxgen.rdsource.helpers

import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import java.nio.file.Path
import java.util.Comparator

internal object NumberAwareFilenameComparator : Comparator<Path> {

    private val stringComparator = NumberAwareStringComparator.instance()

    fun instance() = this

    override fun compare(path1: Path, path2: Path): Int {
        return stringComparator.compare(path1.fileName.toString(), path2.fileName.toString())
    }
}
