package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfig
import fi.vm.yti.taxgen.rdsprovider.config.DpmSourceConfigInput
import java.nio.file.Path

class DpmSourceRdsAdapter(
    configPath: Path,
    private val diagnostic: Diagnostic
) : DpmSource() {

    private val configFilePath = configPath.toAbsolutePath().normalize()

    private val loadedConfig: LoadedConfig by lazy(this::loadConfig)

    private data class LoadedConfig(
        val configData: String,
        val config: DpmSourceConfig
    )

    override fun contextLabel(): String = "Reference Data service"
    override fun contextIdentifier(): String = configFilePath.toString()

    override fun sourceConfigData(): String = loadedConfig.configData

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        return loadedConfig.config.dpmDictionaries.map { config ->
            DpmDictionarySourceRdsAdapter(
                config,
                diagnostic
            )
        }
    }

    override fun close() {
    }

    private fun loadConfig(): LoadedConfig {
        return diagnostic.withContext(
            contextType = DiagnosticContextType.InitConfiguration,
            contextIdentifier = configFilePath.fileName.toString()
        ) {
            val configInputData = FileOps.readTextFile(configFilePath)

            val configInput = JsonOps.readValue<DpmSourceConfigInput>(configInputData, diagnostic)

            val config = configInput.toValidConfig(diagnostic)

            LoadedConfig(
                configData = configInputData,
                config = config
            )
        }
    }
}
