package fi.vm.yti.taxgen.cli

import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,

    val cmdProduceDpmDb: Path?,
    val cmdCaptureYclSourcesToFolder: Path?,
    val cmdCaptureYclSourcesToZip: Path?,

    val forceOverwrite: Boolean,

    val sourceConfigFile: Path?,
    val sourceFolder: Path?,
    val sourceZipFile: Path?
) {

    fun ensureSingleCommandGiven() {
        val targetOptionCount = listOf<Any?>(
            cmdProduceDpmDb,
            cmdCaptureYclSourcesToFolder,
            cmdCaptureYclSourcesToZip
        ).count { it != null }

        if (targetOptionCount != 1) {
            haltWithError("Single command with proper argument must be given")
        }
    }

    fun ensureSingleSourceGiven() {
        val sourceOptionCount = listOf<Any?>(
            sourceConfigFile,
            sourceFolder,
            sourceZipFile
        ).count { it != null }

        if (sourceOptionCount != 1) {
            haltWithError("Single source with proper argument must be given")
        }
    }
}
