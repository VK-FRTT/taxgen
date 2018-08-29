package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopic
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import fi.vm.yti.taxgen.commons.JsonOps
import okhttp3.HttpUrl

class YclPaginationAwareResourceIterator(
    url: HttpUrl,
    val diagnostic: Diagnostic,
    val resourceKind: String
) : AbstractIterator<String>() {

    private var nextPageUrl: HttpUrl? = composeInitialPagedUrl(url)
    private var index: Int = 0

    override fun computeNext() {
        val url = nextPageUrl

        if (url != null) {
            val topic = DiagnosticTopic(
                type = "$resourceKind Load",
                identifier = "#$index"
            )

            diagnostic.withinTopic(topic) {
                val resource = HttpOps.fetchJsonData(url, diagnostic)
                resolveNextPageUrl(resource)
                index++
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
            HttpUrl.parse(rawNextPageUrl) ?: diagnostic.fatal("Malformed code list URL: $rawNextPageUrl")
        }
    }
}
