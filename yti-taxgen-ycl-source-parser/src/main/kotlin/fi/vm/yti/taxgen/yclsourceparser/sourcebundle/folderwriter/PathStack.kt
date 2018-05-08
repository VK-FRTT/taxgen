package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter

import java.nio.file.Path
import java.util.LinkedList

class PathStack(private val rootPath: Path) {

    private val elementStack = LinkedList<String>()

    fun pushSubfolderWithIndex(subfolderName: String, index: Int) {
        elementStack.push("${subfolderName}_$index")
    }

    fun pop() {
        elementStack.pop()
    }

    fun currentPath(): Path {
        return elementStack.foldRight(rootPath) { element, path ->
            path.resolve(element)
        }
    }

    fun resolvePath(filename: String): Path {
        return currentPath().resolve(filename)
    }
}
