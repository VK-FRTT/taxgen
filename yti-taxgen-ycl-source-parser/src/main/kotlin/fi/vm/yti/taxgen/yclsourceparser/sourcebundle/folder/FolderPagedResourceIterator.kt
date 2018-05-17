package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import java.nio.file.Path

class FolderPagedResourceIterator(
    path: Path,
    filenameGlob: String
) : AbstractIterator<String>() {

    private val filePaths = FileOps.listFilesMatching(path, filenameGlob).iterator()

    override fun computeNext() {
        if (filePaths.hasNext()) {
            val fileContent = FileOps.readTextFile(filePaths.next())
            setNext(fileContent)
        } else {
            done()
        }
    }
}
