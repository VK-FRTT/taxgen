package fi.vm.yti.taxgen.yclsourceprovider.helpers

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper.lenientObjectMapper
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import java.io.BufferedReader
import java.io.BufferedWriter
import java.nio.charset.StandardCharsets
import java.nio.file.DirectoryStream
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object FileOps {

    private val fileCharset = StandardCharsets.UTF_8

    fun listSubFoldersMatching(parentFolderPath: Path, subFolderGlob: String): List<Path> {
        val adaptedSubFolderGlob = adaptSubFolderGlobToFileSystem(subFolderGlob, parentFolderPath.fileSystem)

        val directoryStream = createDirectoryStream(parentFolderPath, adaptedSubFolderGlob)

        return directoryStream.use { it.filter { path -> Files.isDirectory(path) } }
    }

    private fun adaptSubFolderGlobToFileSystem(subFolderGlob: String, targetFileSystem: FileSystem): String {
        return when (targetFileSystem.provider().scheme) {
            "jar" -> "$subFolderGlob/"
            "file" -> subFolderGlob
            else -> {
                thisShouldNeverHappen("Unsupported filesystem kind")
            }
        }
    }

    fun listFilesMatching(parentFolderPath: Path, filenameGlob: String): List<Path> {
        val directoryStream = createDirectoryStream(parentFolderPath, filenameGlob)
        return directoryStream.use { it.filter { path -> Files.isRegularFile(path) } }
    }

    inline fun <reified T : Any> readJsonFileAsObject(filePath: Path): T {
        val mapper = lenientObjectMapper()
        val reader: BufferedReader = createBufferedReader(filePath)
        return reader.use { mapper.readValue(it) }
    }

    fun readTextFile(path: Path, filename: String): String {
        val filePath = path.resolve(filename)
        return readTextFile(filePath)
    }

    fun readTextFile(filePath: Path): String {
        val reader = createBufferedReader(filePath)

        return reader.use {
            it.readText()
        }
    }

    fun writeTextFile(content: String, pathStack: PathStack, filename: String, forceOverwrite: Boolean) {
        val filePath = pathStack.resolveFilePath(filename)
        val writer = createBufferedWriter(filePath, forceOverwrite)

        writer.use {
            it.write(content)
        }
    }

    fun createDirectoryStream(directory: Path, glob: String): DirectoryStream<Path> {
        require(Files.isDirectory(directory)) { "Given path $directory is not a directory" }

        return Files.newDirectoryStream(directory, glob)
    }

    fun createBufferedReader(filePath: Path): BufferedReader {
        return Files.newBufferedReader(filePath, fileCharset)
    }

    fun createBufferedWriter(filePath: Path, forceOverwrite: Boolean): BufferedWriter {
        return if (forceOverwrite) {
            Files.newBufferedWriter(filePath, fileCharset)
        } else {
            Files.newBufferedWriter(filePath, fileCharset, StandardOpenOption.CREATE_NEW)
        }
    }
}
