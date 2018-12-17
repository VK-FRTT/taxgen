package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.throwFail
import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,

    val cmdCompileDpmDb: Path?,
    val cmdCaptureDpmSourcesToFolder: Path?,
    val cmdCaptureDpmSourcesToZip: Path?,

    val forceOverwrite: Boolean,

    val sourceConfigFile: Path?,
    val sourceFolder: Path?,
    val sourceZipFile: Path?
) {

    fun ensureSingleCommandGiven() {
        val commandCount = listOf<Any?>(
            cmdCompileDpmDb,
            cmdCaptureDpmSourcesToFolder,
            cmdCaptureDpmSourcesToZip
        ).count { it != null }

        if (commandCount != 1) {
            throwFail("Single command with proper argument must be given")
        }
    }

    fun ensureSingleSourceGiven() {
        val sourceOptionCount = listOf<Any?>(
            sourceConfigFile,
            sourceFolder,
            sourceZipFile
        ).count { it != null }

        if (sourceOptionCount != 1) {
            throwFail("Single source with proper argument must be given")
        }
    }
}
