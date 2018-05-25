package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers

import fi.vm.yti.taxgen.commons.ext.kotlin.whenNotNull
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.YclServiceCodeList
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.Closeable

object HttpOps : Closeable {

    private var httpClient: OkHttpClient? = null

    fun getJsonData(url: String): String {
        val request = Request.Builder()
            .get()
            .url(url)
            .header("Accept", "application/json")
            .build()

        val response = httpClient().newCall(request).execute()

        if (!response.isSuccessful) {
            throw YclServiceCodeList.InitFailException()
        }

        return response.body().use {
            it!!.string()
        }
    }

    override fun close() {
        httpClient.whenNotNull { it.dispatcher().executorService().shutdown() }
        httpClient = null
    }

    private fun httpClient(): OkHttpClient {
        return httpClient ?: createHttpClient().also { httpClient = it }
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient().newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun useHttpClient(httpClient: OkHttpClient) {
        close()

        this.httpClient = httpClient
    }
}
