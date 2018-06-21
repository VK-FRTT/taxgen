package fi.vm.yti.taxgen.yclsourceprovider.helpers

import java.nio.file.Files
import java.nio.file.Path
import java.util.LinkedList

class PathStack(
    private val baseFolderPath: Path,
    private val createFileSystemPaths: Boolean = false
) {
    private val subFolderStack = LinkedList<String>()
    private var currentPathCache: Path? = null

    init {
        createFileSystemPath()
    }

    fun withIndexPostfixSubfolder(subfolderName: String, index: Int, block: () -> Unit) {
        pushSubfolderWithIndex(subfolderName, index)
        block()
        popSubfolder()
    }

    private fun pushSubfolderWithIndex(subfolderName: String, index: Int) {
        subFolderStack.push("${subfolderName}_$index")
        resetCache()
        createFileSystemPath()
    }

    private fun popSubfolder() {
        subFolderStack.pop()
        resetCache()
    }

    fun currentPath(): Path {
        return currentPathCache ?: buildCurrentPath()
    }

    fun resolveFilePath(filename: String): Path {
        return currentPath().resolve(filename)
    }

    private fun buildCurrentPath(): Path {
        return subFolderStack
            .foldRight(baseFolderPath) { element, path -> path.resolve(element) }
            .also { currentPathCache = it }
    }

    private fun resetCache() {
        currentPathCache = null
    }

    private fun createFileSystemPath() {
        if (createFileSystemPaths) Files.createDirectories(currentPath())
    }
}
