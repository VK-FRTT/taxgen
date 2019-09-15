package fi.vm.yti.taxgen.commons.ops

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

    fun withSubfolder(subfolderName: String, block: () -> Unit) {
        enterSubfolder(subfolderName)
        block()
        exitSubfolder()
    }

    private fun enterSubfolder(subfolderName: String) {
        subFolderStack.push(subfolderName)
        createFileSystemPath(combineFilesystemPath())
    }

    private fun exitSubfolder() {
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
