package fi.vm.yti.taxgen.cli

import java.io.File

data class DetectedOptions(
    val help: Boolean,
    val yclConfigFile: File?
)
