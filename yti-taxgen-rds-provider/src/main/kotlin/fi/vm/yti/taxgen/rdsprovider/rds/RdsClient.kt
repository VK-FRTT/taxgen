package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import okhttp3.HttpUrl
import okhttp3.Request

internal class RdsClient(
    private val diagnostic: Diagnostic
) {
    fun fetchJsonAsNodeTree(
        url: HttpUrl,
        extraQueryParams: List<Pair<String, String>> = emptyList(),
        requestPrettyJson: Boolean = true
    ): JsonNode {

        val jsonString = fetchJsonAsString(url, extraQueryParams, requestPrettyJson)
        return JsonOps.readTree(jsonString, diagnostic)
    }

    fun fetchJsonAsString(
        url: HttpUrl,
        extraQueryParams: List<Pair<String, String>> = emptyList(),
        requestPrettyJson: Boolean = true
    ): String {

        val urlBuilder = url.newBuilder()

        if (requestPrettyJson) {
            urlBuilder.setQueryParameter("pretty", null)
        }

        extraQueryParams.forEach {
            urlBuilder.addQueryParameter(it.first, it.second)
        }

        val decoratedUrl = urlBuilder.build()

        return executeJsonGet(decoratedUrl)
    }

    private fun executeJsonGet(
        url: HttpUrl
    ): String {

        val request = Request.Builder()
            .get()
            .url(url)
            .header("Accept", "application/json")
            .build()

        val response = try {
            HttpClientHolder.httpClient().newCall(request).execute()
        } catch (e: java.net.UnknownHostException) {
            diagnostic.fatal("Could not determine the server IP address. Url: $url")
        } catch (e: java.net.ConnectException) {
            diagnostic.fatal("Could not connect the server. Url: $url")
        } catch (e: java.net.SocketTimeoutException) {
            diagnostic.fatal("The server communication timeout. Url: $url")
        } catch (e: java.net.SocketException) {
            diagnostic.fatal("The server communication failed. ${e.message} Url: $url")
        }

        if (!response.isSuccessful) {
            diagnostic.fatal("JSON content fetch failed: HTTP ${response.code()} (${response.message()})")
        }

        return response.body().use {
            it ?: thisShouldNeverHappen("HTTP response body missing")

            it.string()
        }
    }
}
