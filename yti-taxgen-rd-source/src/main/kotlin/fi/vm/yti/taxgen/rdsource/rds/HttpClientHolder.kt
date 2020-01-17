package fi.vm.yti.taxgen.rdsource.rds

import java.io.Closeable
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

internal object HttpClientHolder : Closeable {

    private var httpClient: OkHttpClient? = null

    override fun close() {
        httpClient?.let { it.dispatcher.executorService.shutdown() }
        httpClient = null
    }

    fun httpClient(): OkHttpClient {
        return httpClient ?: createHttpClient().also { httpClient = it }
    }

    private fun createHttpClient(): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun useCustomHttpClient(httpClient: OkHttpClient) {
        close()
        HttpClientHolder.httpClient = httpClient
    }
}
