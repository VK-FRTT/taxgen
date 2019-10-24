package fi.vm.yti.taxgen.rdsprovider.configdata

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ops.FileOps
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsprovider.DpmSourceConfigHolder
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
            val content = FileOps.readTextFile(configFilePath)

            val configData = JsonOps.readValue<DpmSourceConfigData>(
                content,
                diagnosticContext
            )

            val dpmSourceConfig = configData.toDpmSourceConfig(diagnosticContext)
            val processingOptions = configData.toProcessingOptions(diagnosticContext)

            processingOptions.emitDiagnostics(diagnosticContext)

            DpmSourceConfigHolder(
                configFilePath = configFilePath.toString(),
                configData = content,
                dpmSourceConfig = dpmSourceConfig,
                processingOptions = processingOptions
            )
        }
    }

    fun ownerFromFile(
        configFilePath: Path,
        diagnostic: Diagnostic
    ): Owner {
        val content = FileOps.readTextFile(configFilePath)

        val configData = JsonOps.readValue<OwnerConfigData>(
            content,
            diagnostic
        )

        return configData.toOwner(diagnostic)
    }

    fun ownerToJsonString(owner: Owner): String {
        val configData = OwnerConfigData.fomOwner(owner)
        return JsonOps.writeAsJsonString(configData)
    }
}
