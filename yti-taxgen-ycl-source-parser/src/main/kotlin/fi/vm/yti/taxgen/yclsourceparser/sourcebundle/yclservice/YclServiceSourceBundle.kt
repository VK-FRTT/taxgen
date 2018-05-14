package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.kotlin.toJsonString
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.BundleDescriptor
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.SourceConfig
import okhttp3.OkHttpClient
import java.nio.file.Path
import java.time.Instant

class YclServiceSourceBundle(
    sourceConfigFilePath: Path
) : SourceBundle {

    private val sourceConfigFilePath = sourceConfigFilePath.toAbsolutePath().normalize()
    private val yclSourceConfig = readSourceConfig()
    private val bundleDescriptor = initBundleDescriptor()
    private val httpClient = createHttpClient()

    private fun readSourceConfig(): SourceConfig {
        return FileOps.readJsonFileAsObject(sourceConfigFilePath)
    }

    private fun initBundleDescriptor(): String {
        val descriptor = BundleDescriptor(
            createdAt = Instant.now().toString()
        )

        return descriptor.toJsonString()
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    override fun bundleDescriptor(): String = bundleDescriptor

    override fun taxonomyUnits(): List<TaxonomyUnit> {
        return yclSourceConfig.taxonomyUnits.map { YclServiceTaxonomyUnit(it, httpClient) }
    }

    override fun close() {
        httpClient.dispatcher().executorService().shutdown()
    }
}
