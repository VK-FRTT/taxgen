package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import okhttp3.HttpUrl

internal class YclPaginationAwareResourceIterator(
    url: HttpUrl,
    private val diagnostic: Diagnostic,
    private val diagnosticContextType: DiagnosticContextType
) : AbstractIterator<String>() {

    private var nextPageUrl: HttpUrl? = composeInitialPagedUrl(url)

    override fun computeNext() {
        val url = nextPageUrl

        if (url != null) {
            diagnostic.withContext(
                contextType = diagnosticContextType,
                contextIdentifier = ""
            ) {
                val resource = HttpOps.fetchJsonData(url, diagnostic)
                resolveNextPageUrl(resource)
                setNext(resource)
            }
        } else {
            done()
        }
    }

    private fun composeInitialPagedUrl(url: HttpUrl): HttpUrl =
        url.newBuilder().addQueryParameter("pageSize", "1000").build()

    private fun resolveNextPageUrl(response: String) {
        val responseJson = JsonOps.readTree(response, diagnostic)

        val rawNextPageUrl = responseJson.nonBlankTextOrNullAt("/meta/nextPage")

        nextPageUrl = if (rawNextPageUrl == null) {
            null
        } else {
            HttpUrl.parse(rawNextPageUrl) ?: diagnostic.fatal("Malformed page URL: $rawNextPageUrl")
        }
    }
}
