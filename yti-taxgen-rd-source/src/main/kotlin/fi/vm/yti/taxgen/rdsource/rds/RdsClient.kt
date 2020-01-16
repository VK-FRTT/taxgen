package fi.vm.yti.taxgen.rdsource.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import okhttp3.HttpUrl
import okhttp3.Request
import okhttp3.Response

val RETRYABLE_STATUS_CODES = listOf(500, 502, 503, 504)
const val MAX_RETRIES = 3

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

        (1..MAX_RETRIES).forEach { index ->
            val result = HttpClientHolder.httpClient().newCall(request).runCatching { execute() }

            result.onFailure { throwable ->
                emitDiagnosticOnThrow(throwable, url)
            }

            result.onSuccess { response ->

                if (index != MAX_RETRIES && retryableHttpFail(response)) {
                    diagnostic.debug("Server error: ${response.statusPhrase()}, retrying $index")
                    return@forEach
                }

                emitDiagnosticOnHttpFail(response)

                return response.body.use { body ->
                    body ?: thisShouldNeverHappen("HTTP response body missing.")
                    body.string()
                }
            }
        }

        thisShouldNeverHappen("Retry logic mismatch")
    }

    private fun retryableHttpFail(response: Response): Boolean {
        return response.code in RETRYABLE_STATUS_CODES
    }

    private fun emitDiagnosticOnHttpFail(response: Response) {
        if (!response.isSuccessful) {
            diagnostic.fatal(
                "JSON content fetch failed: ${response.statusPhrase()}"
            )
        }
    }

    private fun emitDiagnosticOnThrow(throwable: Throwable, url: HttpUrl): Nothing {
        val diagnosticMessage = diagnosticMessageForThrowable(throwable, url)

        if (diagnosticMessage != null) {
            diagnostic.fatal(diagnosticMessage)
        } else {
            throw throwable
        }
    }

    private fun diagnosticMessageForThrowable(throwable: Throwable, url: HttpUrl): String? {
        return when (throwable) {
            is java.net.UnknownHostException -> "Could not determine the server IP address. Url: $url"
            is java.net.ConnectException -> "Could not connect the server. Url: $url"
            is java.net.SocketTimeoutException -> "The server communication timeout. Url: $url"
            is java.net.SocketException -> "The server communication failed. ${throwable.message} Url: $url"
            else -> null
        }
    }

    private fun Response.statusPhrase(): String {
        val codeText = when (code) {
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

        return "HTTP $code ($codeText)"
    }
}
