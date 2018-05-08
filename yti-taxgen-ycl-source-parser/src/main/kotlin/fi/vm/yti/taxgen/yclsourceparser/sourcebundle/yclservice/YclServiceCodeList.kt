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

    private val codeListUrls = resolveCodeListUrls(yclCodeListConfig.uri)

    override fun codeListData(): String? {
        return withCodeListUrls {
            it.codeListData
        }
    }

    override fun codesData(): String? {
        return withCodeListUrls {
            fetch(it.codesUrl)
        }
    }

    private fun resolveCodeListUrls(codeListUri: String): CodeListUrls? {
        val codeListData = fetch(codeListUri) ?: return null
        val codeListJson = jacksonObjectMapper().readTree(codeListData) ?: return null
        val codesUrl = codeListJson.nonBlankTextOrNullAt("/codesUrl") ?: return null

        return CodeListUrls(
            codeListData = codeListData,
            codesUrl = codesUrl
        )
    }

    private fun withCodeListUrls(action: (CodeListUrls) -> String?): String? {
        if (codeListUrls != null) {
            return action(codeListUrls)
        }

        return null
    }

    private fun fetch(url: String): String? {
        val httpClient = OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/json")
            .build()

        val response = httpClient.newCall(request).execute()

        if (response.isSuccessful) {
            return response.body().use {
                it!!.string()
            }
        }

        return null
    }
}
