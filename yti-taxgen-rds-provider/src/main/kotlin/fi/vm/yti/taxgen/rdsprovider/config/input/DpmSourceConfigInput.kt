package fi.vm.yti.taxgen.rdsprovider.config.input

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig
import java.nio.file.Path

@Suppress("MemberVisibilityCanBePrivate")
data class DpmSourceConfigInput(
    val dpmDictionaries: List<DpmDictionarySourceConfigInput?>?
) {
    companion object {
        fun tryReadAndValidateConfig(
            configFilePath: Path,
            diagnostic: Diagnostic
        ): DpmSourceConfig? {
            val configInputData = FileOps.readTextFile(configFilePath)

            val configInput = JsonOps.readValue<DpmSourceConfigInput>(configInputData, diagnostic)

            configInput.validate(diagnostic)

            return DpmSourceConfig(
                configFilePath = configFilePath.toString(),
                rawConfigData = configInputData,
                dpmDictionaries = configInput.dpmDictionaries!!.map {
                    it!!.toConfig(diagnostic)
                }
            )
        }
    }

    fun validate(diagnostic: Diagnostic) {
        validateValueNotNull(this::dpmDictionaries, diagnostic)
        validateListElementsNotNull(this::dpmDictionaries, diagnostic)
    }
}
