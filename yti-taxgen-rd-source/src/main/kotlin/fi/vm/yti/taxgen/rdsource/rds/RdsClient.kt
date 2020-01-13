package fi.vm.yti.taxgen.rdsource.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
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

        diagnostic.debug("Fetching JSON: $url")

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
            diagnostic.fatal(
                "JSON content fetch failed: HTTP ${response.code} (${fixedReasonPhraseForStatusCode(
                    response.code
                )})"
            )
        }

        return response.body.use {
            it ?: thisShouldNeverHappen("HTTP response body missing")

            it.string()
        }
    }

    private fun fixedReasonPhraseForStatusCode(code: Int): String {
        return when (code) {
            100 -> "Continue"
            101 -> "Switching Protocols"
            200 -> "OK"
            201 -> "Created"
            202 -> "Accepted"
            203 -> "Non-Authoritative Information"
            204 -> "No Content"
            205 -> "Reset Content"
            206 -> "Partial Content"
            300 -> "Multiple Choices"
            301 -> "Moved Permanently"
            302 -> "Found"
            303 -> "See Other"
            304 -> "Not Modified"
            305 -> "Use Proxy"
            307 -> "Temporary Redirect"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            402 -> "Payment Required"
            403 -> "Forbidden"
            404 -> "Not Found"
            405 -> "Method Not Allowed"
            406 -> "Not Acceptable"
            407 -> "Proxy Authentication Required"
            408 -> "Request Time-out"
            409 -> "Conflict"
            410 -> "Gone"
            411 -> "Length Required"
            412 -> "Precondition Failed"
            413 -> "Request Entity Too Large"
            414 -> "Request-URI Too Large"
            415 -> "Unsupported Media Type"
            416 -> "Requested range not satisfiable"
            417 -> "Expectation Failed"
            500 -> "Internal Server Error"
            501 -> "Not Implemented"
            502 -> "Bad Gateway"
            503 -> "Service Unavailable"
            504 -> "Gateway Time-out"
            505 -> "HTTP Version not supported"

            else -> ""
        }
    }
}
