package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.BundleInfo
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclSourceConfig
import java.nio.file.Path
import java.time.Instant

class YclServiceSourceBundle(sourceConfigFile: Path) :
    SourceBundle {

    private val sourceBundleInfo = BundleInfo(
        sourceBundleType = "YclServiceSourceBundle",
        createdAt = Instant.now().toString()
    )

    private val yclSourceConf = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }.readValue<YclSourceConfig>(sourceConfigFile.toFile())

    override fun bundleInfo(): BundleInfo = sourceBundleInfo

    override fun taxonomyUnits(): Iterator<TaxonomyUnit> {
        return YclServiceTaxonomyUnitIterator(yclSourceConf.taxonomyUnits)
    }

    override fun close() {
    }
}
