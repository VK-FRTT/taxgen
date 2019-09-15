package fi.vm.yti.taxgen.rdsprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class DpmSourceConfigInput(
    val dpmDictionaries: List<DpmDictionarySourceConfigInput?>?,
    val processingOptions: ProcessingOptionsConfigInput?
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
