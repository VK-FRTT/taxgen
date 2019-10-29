package fi.vm.yti.taxgen.rdsprovider.configdata

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class DpmSourceConfigData(
    val dpmDictionaries: List<DpmDictionarySourceConfigData?>?,
    val processingOptions: ProcessingOptionsConfigData?
) {
    fun toDpmSourceConfig(diagnostic: Diagnostic): DpmSourceConfig {
        validateValueNotNull(this::dpmDictionaries, diagnostic)
        validateListElementsNotNull(this::dpmDictionaries, diagnostic)

        return DpmSourceConfig(
            dpmDictionaryConfigs = dpmDictionaries!!.map {
                it!!.toConfig(diagnostic)
            }
        )
    }

    fun toProcessingOptions(diagnostic: Diagnostic): ProcessingOptions {
        validateValueNotNull(this::processingOptions, diagnostic)

        return processingOptions!!.toProcessingOptions(diagnostic)
    }
}
