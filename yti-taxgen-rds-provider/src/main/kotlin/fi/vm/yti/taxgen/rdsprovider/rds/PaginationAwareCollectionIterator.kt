package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.ops.JsonOps
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import okhttp3.HttpUrl

internal class PaginationAwareCollectionIterator(
    collectionUrl: HttpUrl,
    private val rdsClient: RdsClient,
    private val diagnostic: Diagnostic,
    private val iterationObserver: IterationObserver = NullObserver()
) : AbstractIterator<String>() {

    interface IterationObserver {
        fun iteratedPage(pageJson: JsonNode)
        fun iterationDone()
    }

    class NullObserver : IterationObserver {
        override fun iteratedPage(pageJson: JsonNode) {}
        override fun iterationDone() {}
    }

    private var nextPageUrl: HttpUrl? = composeInitialPageUrl(collectionUrl)

    override fun computeNext() {
        val pageUrl = nextPageUrl

        if (pageUrl != null) {
            val page = rdsClient.fetchJsonAsString(pageUrl)
            processPage(page)
            setNext(page)
        } else {
            processDone()
        }
    }

    private fun composeInitialPageUrl(url: HttpUrl): HttpUrl =
        url.newBuilder().addQueryParameter("pageSize", "1000").build()

    private fun processPage(page: String) {
        val pageJson = JsonOps.readTree(page, diagnostic)

        nextPageUrl = pageJson.nonBlankTextOrNullAt("/meta/nextPage")?.let {
            HttpUrl.parse(it) ?: diagnostic.fatal("Malformed page URL: $it")
        }

        iterationObserver.iteratedPage(pageJson)
    }

    private fun processDone() {
        iterationObserver.iterationDone()
        done()
    }
}
