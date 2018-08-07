package fi.vm.yti.taxgen.testcommons

import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class TempFolder(discriminator: String) : Closeable {

    private val rootPath = Files.createTempDirectory(discriminator)

    override fun close() {
        Files
            .walk(rootPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    fun path(): Path {
        return rootPath
    }

    fun resolve(subPath: String): Path {
        return rootPath.resolve(subPath)
    }

    fun copyFolderRecursivelyUnderSubfolder(
        sourceFolderPath: Path,
        subfolder: String
    ): Path {
        val targetRootPath = rootPath.resolve(subfolder)

        Files
            .walk(sourceFolderPath)
            .filter {
                Files.isRegularFile(it)
            }
            .forEach {
                //ZIP FS relativizes only paths which have same absoluteness
                val sourceSubFilePath = sourceFolderPath.toAbsolutePath().relativize(it.toAbsolutePath())

                //Map sourceSubFilePath to target FileSystem by passing it as String to resolve()
                val targetFilePath = targetRootPath.resolve("$sourceSubFilePath")

                Files.createDirectories(targetFilePath.parent)
                Files.copy(it, targetFilePath)
            }

        return targetRootPath
    }

    fun copyFileToSubfolder(
        sourceFilePath: Path,
        subfolder: String
    ): Path {
        val targetRootPath = rootPath.resolve(subfolder)
        Files.createDirectories(targetRootPath)

        val targetFilePath = targetRootPath.resolve("${sourceFilePath.fileName}")
        Files.copy(sourceFilePath, targetFilePath)

        return targetFilePath
    }

    fun createFileWithContent(filename: String, content: String): Path {
        val filepath = rootPath.resolve(filename)
        Files.write(filepath, content.toByteArray(), StandardOpenOption.CREATE_NEW)
        return filepath
    }
}
