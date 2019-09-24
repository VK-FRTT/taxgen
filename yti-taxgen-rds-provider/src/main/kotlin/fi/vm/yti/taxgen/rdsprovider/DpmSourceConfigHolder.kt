package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions

data class DpmSourceConfigHolder(
    val configFilePath: String,
    val configData: String,
    val dpmSourceConfig: DpmSourceConfig,
    val processingOptions: ProcessingOptions
)
