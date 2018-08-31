package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopic
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.api.config.YclSourceApiAdapterConfig
import fi.vm.yti.taxgen.commons.FileOps
import java.nio.file.Path
import java.time.Instant

class YclSourceApiAdapter(
    configPath: Path,
    private val diagnostic: Diagnostic
) : YclSource() {

    private val configFilePath = configPath.toAbsolutePath().normalize()

    private val loadedConfig: LoadedConfig by lazy(this::loadConfig)
    private val sourceInfoData: String by lazy(this::composeSourceInfo)

    private data class LoadedConfig(
        val configData: String,
        val configObjects: YclSourceApiAdapterConfig,
        val configMap: Map<String, Any>
    )

    override fun topicName(): String = "YTI Reference Data service"
    override fun topicIdentifier(): String = configFilePath.toString()

    override fun sourceInfoData(): String = sourceInfoData

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        return loadedConfig.configObjects.dpmDictionaryConfigs.mapIndexed { index, config ->
            DpmDictionarySourceApiAdapter(
                index,
                config,
                diagnostic
            )
        }
    }

    override fun close() {
    }

    private fun loadConfig(): LoadedConfig {
        val topic = DiagnosticTopic(
            type = "Configuration file",
            identifier = configFilePath.fileName.toString()
        )

        return diagnostic.withinTopic(topic) {
            val configData = FileOps.readTextFile(configFilePath)

            LoadedConfig(
                configData = configData,
                configObjects = JsonOps.readValue(configData, diagnostic),
                configMap = JsonOps.readValue(configData, diagnostic)
            )
        }
    }

    private fun composeSourceInfo(): String {
        val info = ApiAdapterSourceInfo(
            createdAt = Instant.now().toString(),
            sourceConfig = loadedConfig.configMap
        )

        return JsonOps.writeAsJsonString(info)
    }
}
