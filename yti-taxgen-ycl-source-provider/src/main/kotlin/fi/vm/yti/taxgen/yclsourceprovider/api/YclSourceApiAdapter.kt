package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.FileOps
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.config.YclSourceConfig
import fi.vm.yti.taxgen.yclsourceprovider.config.input.YclSourceConfigInput
import java.nio.file.Path

class YclSourceApiAdapter(
    configPath: Path,
    private val diagnostic: Diagnostic
) : YclSource() {

    private val configFilePath = configPath.toAbsolutePath().normalize()

    private val loadedConfig: LoadedConfig by lazy(this::loadConfig)

    private data class LoadedConfig(
        val configData: String,
        val config: YclSourceConfig
    )

    override fun contextName(): String = "YTI Reference Data service"
    override fun contextRef(): String = configFilePath.toString()

    override fun sourceConfigData(): String = loadedConfig.configData

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        return loadedConfig.config.dpmDictionaries.map { config ->
            DpmDictionarySourceApiAdapter(
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
            contextRef = configFilePath.fileName.toString()
        ) {
            val configInputData = FileOps.readTextFile(configFilePath)

            val configInput = JsonOps.readValue<YclSourceConfigInput>(configInputData, diagnostic)

            val config = configInput.toValidConfig(diagnostic)

            LoadedConfig(
                configData = configInputData,
                config = config
            )
        }
    }
}
