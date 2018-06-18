package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl

import fi.vm.yti.taxgen.yclsourceparser.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.JacksonObjectMapper
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.ycl.config.CodeListConfig
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
        val uriResolveResultData = HttpOps.getJsonData(codeListConfig.uri)
        val uriResolveResultJson = JacksonObjectMapper.lenientObjectMapper().readTree(uriResolveResultData) ?: throw InitFailException()

        val codeListBaseUrl = uriResolveResultJson.nonBlankTextOrNullAt("/url") ?: throw InitFailException()
        val codeListUrl = HttpUrl.parse(codeListBaseUrl)!!.newBuilder().addQueryParameter("expand", "code").build().toString()

        val codesUrl = uriResolveResultJson.nonBlankTextOrNullAt("/codesUrl") ?: throw InitFailException()

        return ResolvedUrls(
            codeListUrl = codeListUrl,
            codesUrl = codesUrl
        )
    }

    class InitFailException : RuntimeException()
}
