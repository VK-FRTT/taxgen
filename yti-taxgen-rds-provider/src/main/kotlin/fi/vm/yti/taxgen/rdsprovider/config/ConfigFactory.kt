package fi.vm.yti.taxgen.rdsprovider.config

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.rdsprovider.config.input.DpmSourceConfigInput
import java.nio.file.Path

object ConfigFactory {

    fun configFromFile(
        configFilePath: Path,
        diagnostic: Diagnostic
    ): DpmSourceConfigHolder {
        val configData = FileOps.readTextFile(configFilePath)

        val configInput = JsonOps.readValue<DpmSourceConfigInput>(
            configData,
            diagnostic
        )

        return DpmSourceConfigHolder(
            configFilePath = configFilePath.toString(),
            configData = configData,
            dpmSourceConfig = configInput.toDpmSourceConfig(diagnostic),
            processingOptions = configInput.toProcessingOptions(diagnostic)
        )
    }
}
