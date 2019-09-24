package fi.vm.yti.taxgen.rdsprovider.configinput

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfigHolder
import fi.vm.yti.taxgen.rdsprovider.OwnerHolder
import java.nio.file.Path

object ConfigFactory {

    fun dpmSourceConfigFromFile(
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

    fun ownerFromFile(
        configFilePath: Path,
        diagnostic: Diagnostic
    ): OwnerHolder {
        val configData = FileOps.readTextFile(configFilePath)

        val configInput = JsonOps.readValue<OwnerConfigInput>(
            configData,
            diagnostic
        )

        val owner = configInput.toOwner(diagnostic)

        return OwnerHolder(
            configData = configData,
            owner = owner
        )
    }
}
