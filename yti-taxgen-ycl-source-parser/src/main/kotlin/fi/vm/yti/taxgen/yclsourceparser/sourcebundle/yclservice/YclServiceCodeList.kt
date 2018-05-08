package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.yclsourceparser.jacksonextension.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclCodeListConfig
import okhttp3.OkHttpClient
import okhttp3.Request

class YclServiceCodeList(
    yclCodeListConfig: YclCodeListConfig
) : CodeList {

    data class CodeListUrls(
        val codeListData: String,
        val codesUrl: String
    )

    class InitFailException : RuntimeException()

    private val codeListUrls = resolveCodeListUrls(yclCodeListConfig.uri)

    override fun codeListData(): String {
        return codeListUrls.codeListData
    }

    override fun codesData(): String {
        return fetch(codeListUrls.codesUrl)
    }

    private fun resolveCodeListUrls(codeListUri: String): CodeListUrls {
        val codeListData = fetch(codeListUri)
        val codeListJson = jacksonObjectMapper().readTree(codeListData) ?: throw InitFailException()
        val codesUrl = codeListJson.nonBlankTextOrNullAt("/codesUrl") ?: throw InitFailException()

        return CodeListUrls(
            codeListData = codeListData,
            codesUrl = codesUrl
        )
    }

    private fun fetch(url: String): String {
        val httpClient = OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val request = Request.Builder()
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
}
