package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.BundleInfo
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl.config.SourceConfig
import java.nio.file.Path
import java.time.Instant

class YclSourceBundle(
    sourceConfigFilePath: Path
) : SourceBundle {

    private val sourceConfigFilePath = sourceConfigFilePath.toAbsolutePath().normalize()
    private val yclSourceConfig = readSourceConfig()
    private val bundleInfo = initBundleInfo()

    private fun readSourceConfig(): SourceConfig {
        return FileOps.readJsonFileAsObject(sourceConfigFilePath)
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
