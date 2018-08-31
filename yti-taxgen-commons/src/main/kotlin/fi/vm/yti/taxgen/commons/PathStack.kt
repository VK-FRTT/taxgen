package fi.vm.yti.taxgen.commons

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.LinkedList

class PathStack(
    private val baseFolderPath: Path,
    private val createFileSystemPaths: Boolean = false,
    private val diagnostic: Diagnostic
) {
    private val subFolderStack = LinkedList<String>()

    init {
        createFileSystemPath(baseFolderPath)
    }

    fun withIndexPostfixSubfolder(subfolderName: String, index: Int, block: () -> Unit) {
        pushSubfolderWithIndex(subfolderName, index)
        block()
        popSubfolder()
    }

    private fun pushSubfolderWithIndex(subfolderName: String, index: Int) {
        subFolderStack.push("${subfolderName}_$index")

        createFileSystemPath(combineFilesystemPath())
    }

    private fun popSubfolder() {
        subFolderStack.pop()
    }

    fun resolveFilesystemPath(filename: String): Path {
        return combineFilesystemPath().resolve(filename)
    }

    fun resolveDiagnosticPath(filename: String): Path {
        return combineDiagnosticPath().resolve(filename)
    }

    private fun createFileSystemPath(path: Path) {
        if (createFileSystemPaths) {
            try {
                Files.createDirectories(path)
            } catch (e: java.nio.file.FileAlreadyExistsException) {
                diagnostic.fatal("Could not create filesystem path '$path' (already exists)")
            }
        }
    }

    private fun combineFilesystemPath(): Path =
        subFolderStack.foldRight(baseFolderPath) { element, path -> path.resolve(element) }

    private fun combineDiagnosticPath(): Path {
        val base = baseFolderPath.lastOrNull() ?: Paths.get("/")
        return subFolderStack.foldRight(base) { element, path -> path.resolve(element) }
    }
}
