package fi.vm.yti.taxgen.yclsourceprovider.ycl

import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.yclsourceprovider.BundleInfo
import fi.vm.yti.taxgen.yclsourceprovider.SourceBundle
import fi.vm.yti.taxgen.yclsourceprovider.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceprovider.ycl.config.SourceConfig
import java.nio.file.Path
import java.time.Instant

class YclSourceBundle(
    sourceConfigData: String? = null,
    sourceConfigFilePath: Path? = null
) : SourceBundle {

    private val yclSourceConfig = resolveSourceConfig(sourceConfigData, sourceConfigFilePath)
    private val bundleInfo = initBundleInfo()

    private fun resolveSourceConfig(
        sourceConfigData: String?,
        sourceConfigFilePath: Path?
    ): SourceConfig {
        if (sourceConfigData != null) {
            val mapper = JacksonObjectMapper.lenientObjectMapper()
            return mapper.readValue(sourceConfigData)
        }

        if (sourceConfigFilePath != null) {
            val path = sourceConfigFilePath.toAbsolutePath().normalize()
            return FileOps.readJsonFileAsObject(path)
        }

        thisShouldNeverHappen("No configuration provided for YclSourceBundle")
    }

    private fun initBundleInfo(): String {
        val info = BundleInfo(
            createdAt = Instant.now().toString()
        )

        return info.toJsonString()
    }

    override fun bundleInfoData(): String = bundleInfo

    override fun taxonomyUnits(): List<TaxonomyUnit> {
        return yclSourceConfig.taxonomyUnits.map { YclTaxonomyUnit(it) }
    }

    override fun close() {
    }
}