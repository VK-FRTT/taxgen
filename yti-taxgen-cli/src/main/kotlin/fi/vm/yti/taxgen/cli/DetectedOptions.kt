package fi.vm.yti.taxgen.cli

import java.nio.file.Path

data class DetectedOptions(
    val cmdShowHelp: Boolean,
    val cmdGenerateTaxonomy: Boolean,
    val cmdBundleSources: Boolean,

    val yclSourceConfig: Path?,
    val sourceBundleFolder: Path?,
    val sourceBundleZip: Path?,

    val targetFolder: Path?,
    val targetZip: Path?
)
