package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopic
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.api.config.YclCodelistSourceApiAdapterConfig
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import fi.vm.yti.taxgen.commons.JsonOps
import okhttp3.HttpUrl

class YclCodelistSourceApiAdapter(
    index: Int,
    private val config: YclCodelistSourceApiAdapterConfig,
    private val diagnostic: Diagnostic
) : YclCodelistSource(index) {

    private val contentUrls: ContentUrls by lazy(this::resolveContentUrlsFromUri)

    private data class ContentUrls(
        val codeListUrl: HttpUrl,
        val codesUrl: HttpUrl
    )

    override fun yclCodeschemeData(): String {
        return HttpOps.fetchJsonData(contentUrls.codeListUrl, diagnostic)
    }

    override fun yclCodePagesData(): Iterator<String> {
        return YclPaginationAwareResourceIterator(
            contentUrls.codesUrl,
            diagnostic,
            "Codes Page")
    }

    private fun resolveContentUrlsFromUri(): ContentUrls {
        val topic = DiagnosticTopic(
            type = "URI Resolution",
            identifier = config.uri
        )

        return diagnostic.withinTopic(topic) {
            val uri = HttpUrl.parse(config.uri) ?: diagnostic.fatal("Malformed URI")
            val resolutionData = HttpOps.fetchJsonData(uri, diagnostic)
            val resolutionJson = JsonOps.readTree(resolutionData, diagnostic)

            val codeListRawUrl = resolutionJson.nonBlankTextAt("/url", diagnostic)
            val codeListUrl =
                HttpUrl.parse(codeListRawUrl) ?: diagnostic.fatal("Malformed code list URL: $codeListRawUrl")
            val codeListUrlWithExpand = codeListUrl.newBuilder().addQueryParameter("expand", "code").build()

            val codesRawUrl = resolutionJson.nonBlankTextAt("/codesUrl", diagnostic)
            val codesUrl = HttpUrl.parse(codesRawUrl) ?: diagnostic.fatal("Malformed codes URL: $codesRawUrl")

            ContentUrls(
                codeListUrl = codeListUrlWithExpand,
                codesUrl = codesUrl
            )
        }
    }
}
