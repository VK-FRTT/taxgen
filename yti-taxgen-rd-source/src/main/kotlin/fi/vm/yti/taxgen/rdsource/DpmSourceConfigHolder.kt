package fi.vm.yti.taxgen.rdsource

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions

data class DpmSourceConfigHolder(
    val configFilePath: String,
    val configData: String,
    val dpmSourceConfig: DpmSourceConfig,
    val processingOptions: ProcessingOptions
)
