package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import okhttp3.HttpUrl

class YclCodelistSourceApiAdapter(
    index: Int,
    private val config: YclCodelistSourceApiAdapterConfig
) : YclCodelistSource(index) {

    private var resolvedUrlsCache: ResolvedUrls? = null

    data class ResolvedUrls(
        val codeListUrl: String,
        val codesUrl: String
    )

    override fun yclCodeschemeData(): String {
        val urls = resolvedUrls()
        return HttpOps.getJsonData(urls.codeListUrl)
    }

    override fun yclCodePagesData(): Iterator<String> {
        val urls = resolvedUrls()
        return YclPaginationAwareResourceIterator(urls.codesUrl)
    }

    private fun resolvedUrls(): ResolvedUrls {
        return resolvedUrlsCache ?: resolveUrls().also { resolvedUrlsCache = it }
    }

    private fun resolveUrls(): ResolvedUrls {
        val uriResolutionData = HttpOps.getJsonData(config.uri)
        val uriResolutionJson =
            JacksonObjectMapper.lenientObjectMapper().readTree(uriResolutionData) ?: throw InitFailException() //TODO

        val codeListBaseUrl = uriResolutionJson.nonBlankTextOrNullAt("/url") ?: throw InitFailException()
        val codeListUrl =
            HttpUrl.parse(codeListBaseUrl)!!.newBuilder().addQueryParameter("expand", "code").build().toString()

        val codesUrl = uriResolutionJson.nonBlankTextOrNullAt("/codesUrl") ?: throw InitFailException()

        return ResolvedUrls(
            codeListUrl = codeListUrl,
            codesUrl = codesUrl
        )
    }

    class InitFailException : RuntimeException()
}
