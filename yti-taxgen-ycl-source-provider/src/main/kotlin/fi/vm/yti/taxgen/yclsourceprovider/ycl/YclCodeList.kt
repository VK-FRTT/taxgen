package fi.vm.yti.taxgen.yclsourceprovider.ycl

import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceprovider.CodeList
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceprovider.ycl.config.CodeListConfig
import okhttp3.HttpUrl

class YclCodeList(
    private val codeListConfig: CodeListConfig
) : CodeList {

    private var resolvedUrlsCache: ResolvedUrls? = null

    data class ResolvedUrls(
        val codeListUrl: String,
        val codesUrl: String
    )

    override fun codeListData(): String {
        val urls = resolvedUrls()
        return HttpOps.getJsonData(urls.codeListUrl)
    }

    override fun codePagesData(): Iterator<String> {
        val urls = resolvedUrls()
        return YclPagedResourceRetriever(urls.codesUrl)
    }

    private fun resolvedUrls(): ResolvedUrls {
        return resolvedUrlsCache ?: resolveUrls().also { resolvedUrlsCache = it }
    }

    private fun resolveUrls(): ResolvedUrls {
        val uriResolutionData = HttpOps.getJsonData(codeListConfig.uri)
        val uriResolutionJson =
            JacksonObjectMapper.lenientObjectMapper().readTree(uriResolutionData) ?: throw InitFailException()

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
