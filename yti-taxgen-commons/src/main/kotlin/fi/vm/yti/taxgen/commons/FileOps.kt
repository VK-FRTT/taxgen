package fi.vm.yti.taxgen.commons

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import java.io.BufferedWriter
import java.nio.charset.StandardCharsets
import java.nio.file.DirectoryStream
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object FileOps {

    private val fileCharset = StandardCharsets.UTF_8

    //Create
    fun createIntermediateFolders(targetPath: Path) {
        Files.createDirectories(targetPath.parent)
    }

    fun failIfTargetFileExists(
        targetPath: Path,
        diagnostic: Diagnostic
    ) {
        if (Files.exists(targetPath)) {
            diagnostic.fatal("Target file '$targetPath' already exists")
        }
    }

    fun writeTextFile(
        content: String,
        pathStack: PathStack,
        filename: String,
        forceOverwrite: Boolean,
        diagnostic: Diagnostic
    ) {

        val writer = newBufferedWriter(pathStack, filename, forceOverwrite, diagnostic)

        writer.use {
            it.write(content)
        }

        diagnostic.info("Wrote ${pathStack.resolveDiagnosticPath(filename)}")
    }

    private fun newBufferedWriter(
        pathStack: PathStack,
        filename: String,
        forceOverwrite: Boolean,
        diagnostic: Diagnostic
    ): BufferedWriter {
        val filePath = pathStack.resolveFilesystemPath(filename)

        return if (forceOverwrite) {
            Files.newBufferedWriter(filePath, fileCharset)
        } else {
            try {
                Files.newBufferedWriter(filePath, fileCharset, StandardOpenOption.CREATE_NEW)
            } catch (e: java.nio.file.FileAlreadyExistsException) {
                diagnostic.fatal("Target file '${pathStack.resolveDiagnosticPath(filename)}' already exists")
            }
        }
    }

    //Read
    fun readTextFile(path: Path, filename: String): String {
        val filePath = path.resolve(filename)
        return readTextFile(filePath)
    }

    fun readTextFile(filePath: Path): String {
        val reader = Files.newBufferedReader(filePath, fileCharset)

        return reader.use {
            it.readText()
        }
    }

    //List
    fun listSubFoldersMatching(parentFolderPath: Path, subFolderGlob: String): List<Path> {
        val adjustedGlob = adjustGlobToFileSystem(subFolderGlob, parentFolderPath.fileSystem)
        val directoryStream = newDirectoryStream(parentFolderPath, adjustedGlob)

        return directoryStream.use { it.filter { path -> Files.isDirectory(path) } }
    }

    fun listFilesMatching(parentFolderPath: Path, filenameGlob: String): List<Path> {
        val directoryStream = newDirectoryStream(parentFolderPath, filenameGlob)
        return directoryStream.use { it.filter { path -> Files.isRegularFile(path) } }
    }

    private fun adjustGlobToFileSystem(subFolderGlob: String, targetFileSystem: FileSystem): String {
        return when (targetFileSystem.provider().scheme) {
            "jar" -> "$subFolderGlob/"
            "file" -> subFolderGlob
            else -> {
                thisShouldNeverHappen("Unsupported filesystem kind")
            }
        }
    }

    private fun newDirectoryStream(directory: Path, glob: String): DirectoryStream<Path> {
        require(Files.isDirectory(directory)) { "Given path $directory is not a directory" }
        return Files.newDirectoryStream(directory, glob)
    }

    //Delete
    fun deleteConflictingTargetFileIfAllowed(
        targetPath: Path,
        allowed: Boolean
    ) {
        if (allowed && Files.isRegularFile(targetPath)) {
            Files.delete(targetPath)
        }
    }
}
