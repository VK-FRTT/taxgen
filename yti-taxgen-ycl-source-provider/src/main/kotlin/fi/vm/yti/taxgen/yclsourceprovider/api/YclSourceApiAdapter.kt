package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceprovider.DpmDictionarySource
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import java.nio.file.Path
import java.time.Instant

class YclSourceApiAdapter(
    configPath: Path
) : YclSource() {

    private val configFilePath = configPath.toAbsolutePath().normalize()
    private val configData = FileOps.readTextFile(configFilePath)
    private val config = deserializeConfig()
    private val sourceInfoData = createSourceInfoData()

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

    override fun topicIdentifier(): String = configFilePath.toString()

    override fun sourceInfoData(): String = sourceInfoData

    override fun dpmDictionarySources(): List<DpmDictionarySource> {
        return config.dpmDictionaryConfigs.mapIndexed { index, config -> DpmDictionarySourceApiAdapter(index, config) }
    }

    override fun close() {
    }
}
