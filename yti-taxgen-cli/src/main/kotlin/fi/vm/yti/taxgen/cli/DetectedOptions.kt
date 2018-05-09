package fi.vm.yti.taxgen.cli

import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdGenerateYclTaxonomy: Boolean,
    val cmdBundleYclSource: Boolean,

    val sourceConfig: Path?,
    val sourceBundleFolder: Path?,
    val sourceBundleZip: Path?,

    val targetFolder: Path?,
    val targetZip: Path?,
    val targetForceOverwrite: Boolean
) {

    fun ensureSingleOperation() {
        val targetOptionCount = listOf<Any?>(
            cmdGenerateYclTaxonomy,
            cmdBundleYclSource).count { it == true }

        if (targetOptionCount != 1) {
            haltWithError("single taxonomy operation must be given")
        }
    }

    fun ensureSingleSource() {
        val sourceOptionCount = listOf<Any?>(
            sourceConfig,
            sourceBundleFolder,
            sourceBundleZip).count { it != null }

        if (sourceOptionCount != 1) {
            haltWithError("single taxonomy source must be given")
        }
    }

    fun ensureSingleTarget() {
        val targetOptionCount = listOf<Any?>(
            targetFolder,
            targetZip).count { it != null }

        if (targetOptionCount != 1) {
            haltWithError("single target must be given")
        }
    }
}
