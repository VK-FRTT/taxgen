package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers

import java.nio.file.Path
import java.util.Comparator

object NumberAwareFilenameComparator : Comparator<Path> {

    private val stringComparator = NumberAwareStringComparator.instance()

    fun instance() = this

    override fun compare(path1: Path, path2: Path): Int {
        return stringComparator.compare(path1.fileName.toString(), path2.fileName.toString())
    }
}
