package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import java.nio.file.Path
import java.time.Instant

class YclSourceApiAdapter(
    configData: String? = null,
    configFilePath: Path? = null
) : YclSource {

    private val configData = resolveConfigData(configData, configFilePath)
    private val config = deserializeConfig()
    private val sourceInfoData = createSourceInfoData()

    private fun resolveConfigData(
        configData: String?,
        configFilePath: Path?
    ): String {
        if (configData != null) {
            return configData
        }

        if (configFilePath != null) {
            val path = configFilePath.toAbsolutePath().normalize()
            return FileOps.readTextFile(path)
        }

        thisShouldNeverHappen("No configuration provided for YclSourceApiAdapter")
    }

    private fun deserializeConfig(): YclSourceApiAdapterConfig {
        val mapper = JacksonObjectMapper.lenientObjectMapper()
        return mapper.readValue(configData)
    }

    private fun createSourceInfoData(): String {
        val mapper = JacksonObjectMapper.lenientObjectMapper()

        val info = ApiAdapterSourceInfo(
            createdAt = Instant.now().toString(),
            sourceConfig = mapper.readValue(configData)
        )

        return info.toJsonString()
    }

    override fun sourceInfoData(): String = sourceInfoData

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        return config.dpmDictionaryConfigs.map { DpmDictionarySourceApiAdapter(it) }
    }

    override fun close() {
    }
}
