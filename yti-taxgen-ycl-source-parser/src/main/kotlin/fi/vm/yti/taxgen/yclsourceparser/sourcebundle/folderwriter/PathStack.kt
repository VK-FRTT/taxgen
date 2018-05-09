package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folderwriter

import java.nio.file.Path
import java.util.LinkedList

class PathStack(
    private val rootPath: Path,
    private val createFileSystemPaths: Boolean = false
) {
    private val elementStack = LinkedList<String>()
    private var pathCache: Path? = null

    init {
        createFileSystemPath()
    }

    fun pushSubfolderWithIndex(subfolderName: String, index: Int) {
        elementStack.push("${subfolderName}_$index")
        resetCache()
        createFileSystemPath()
    }

    fun pop() {
        elementStack.pop()
        resetCache()
    }

    fun currentPath(): Path {
        return pathCache ?: buildCurrentPath()
    }

    fun resolvePath(filename: String): Path {
        return currentPath().resolve(filename)
    }

    private fun buildCurrentPath(): Path {
        return elementStack
            .foldRight(rootPath) { element, path -> path.resolve(element) }
            .also { pathCache = it }
    }

    private fun resetCache() {
        pathCache = null
    }

    private fun createFileSystemPath() {
        if (createFileSystemPaths) currentPath().toFile().mkdirs()
    }
}
