package fi.vm.yti.taxgen.cli

import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,

    val cmdWriteDpmDb: Path?,
    val cmdBundleYclSourcesToFolder: Path?,
    val cmdBundleYclSourcesToZip: Path?,

    val forceOverwrite: Boolean,

    val sourceConfig: Path?,
    val sourceBundleFolder: Path?,
    val sourceBundleZip: Path?
) {

    fun ensureSingleCommandGiven() {
        val targetOptionCount = listOf<Any?>(
            cmdWriteDpmDb,
            cmdBundleYclSourcesToFolder,
            cmdBundleYclSourcesToZip
        ).count { it != null }

        if (targetOptionCount != 1) {
            haltWithError("single taxonomy operation must be given")
        }
    }

    fun ensureSingleSourceGiven() {
        val sourceOptionCount = listOf<Any?>(
            sourceConfig,
            sourceBundleFolder,
            sourceBundleZip
        ).count { it != null }

        if (sourceOptionCount != 1) {
            haltWithError("single taxonomy source must be given")
        }
    }
}
