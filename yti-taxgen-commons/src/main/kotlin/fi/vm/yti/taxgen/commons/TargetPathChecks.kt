package fi.vm.yti.taxgen.commons

import java.nio.file.Files
import java.nio.file.Path

object TargetPathChecks {

    fun deleteConflictingTargetFileIfAllowed(
        targetPath: Path,
        allowed: Boolean
    ) {
        if (allowed && Files.isRegularFile(targetPath)) {
            Files.delete(targetPath)
        }
    }

    fun failIfTargetFileExists(targetPath: Path) {
        if (Files.exists(targetPath)) {
            throw IllegalStateException("Unable to create target, file \"${targetPath.fileName}\" already exists")
        }
    }

    fun createIntermediateFolders(targetPath: Path) {
        Files.createDirectories(targetPath.parent)
    }
}
