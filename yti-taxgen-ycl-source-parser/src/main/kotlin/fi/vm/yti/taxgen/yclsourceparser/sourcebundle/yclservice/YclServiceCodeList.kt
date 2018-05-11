package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclCodeListConfig
import okhttp3.OkHttpClient
import okhttp3.Request

class YclServiceCodeList(
    private val yclCodeListConfig: YclCodeListConfig,
    private val httpClient: OkHttpClient
) : CodeList {

    private var resolvedUrlsCache: ResolvedUrls? = null

    data class ResolvedUrls(
        val codeListData: String,
        val codesUrl: String
    )

    override fun codeList(): String {
        val urls = resolvedUrls()
        return urls.codeListData
    }

    override fun codes(): String {
        val urls = resolvedUrls()
        return httpGetJsonData(urls.codesUrl)
    }

    private fun resolvedUrls(): ResolvedUrls {
        return resolvedUrlsCache ?: resolveUrls().also { resolvedUrlsCache = it }
    }

    private fun resolveUrls(): ResolvedUrls {
        val codeListData = httpGetJsonData(yclCodeListConfig.uri)
        val codeListJson = FileOps.lenientObjectMapper().readTree(codeListData) ?: throw InitFailException()
        val codesUrl = codeListJson.nonBlankTextOrNullAt("/codesUrl") ?: throw InitFailException()

        return ResolvedUrls(
            codeListData = codeListData,
            codesUrl = codesUrl
        )
    }

    private fun httpGetJsonData(url: String): String {
        val request = Request.Builder()
            .get()
            .url(url)
            .header("Accept", "application/json")
            .build()

        val response = httpClient.newCall(request).execute()

        if (!response.isSuccessful) {
            throw InitFailException()
        }

        return response.body().use {
            it!!.string()
        }
    }

    class InitFailException : RuntimeException()
}
