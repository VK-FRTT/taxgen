package fi.vm.yti.taxgen.rdsprovider.config

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.rdsprovider.config.input.DpmSourceConfigInput
import java.nio.file.Path

object ConfigFactory {

    fun configFromFile(
        configFilePath: Path,
        diagnosticContext: DiagnosticContext
    ): DpmSourceConfigHolder {
        return diagnosticContext.withContext(
            contextType = DiagnosticContextType.InitConfiguration,
            contextIdentifier = configFilePath.fileName.toString()
        ) {
            val configData = FileOps.readTextFile(configFilePath)

            val configInput = JsonOps.readValue<DpmSourceConfigInput>(
                configData,
                diagnosticContext
            )

            val dpmSourceConfig = configInput.toDpmSourceConfig(diagnosticContext)
            val processingOptions = configInput.toProcessingOptions(diagnosticContext)

            processingOptions.emitDiagnostics(diagnosticContext)

            DpmSourceConfigHolder(
                configFilePath = configFilePath.toString(),
                configData = configData,
                dpmSourceConfig = dpmSourceConfig,
                processingOptions = processingOptions
            )
        }
    }
}
