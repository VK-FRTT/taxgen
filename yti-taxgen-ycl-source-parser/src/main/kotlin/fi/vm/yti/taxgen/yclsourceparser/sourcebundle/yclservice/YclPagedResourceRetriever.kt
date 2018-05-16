package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps

class YclPagedResourceRetriever(
    url: String
) : AbstractIterator<String>() {

    private var nextPageUrl: String? = composeInitialPagedUrl(url)

    override fun computeNext() {
        val url = nextPageUrl

        if (url != null) {
            val resource = HttpOps.getJsonData(url)
            resolveNextPageUrl(resource)
            setNext(resource)
        } else {
            done()
        }
    }

    private fun composeInitialPagedUrl(url: String): String {
        return "$url?pageSize=1000"
    }

    private fun resolveNextPageUrl(response: String) {
        val codeListJson =
            FileOps.lenientObjectMapper().readTree(response) ?: throw YclServiceCodeList.InitFailException()

        val nextPageUrl = codeListJson.nonBlankTextOrNullAt("/meta/nextPage")

        if (nextPageUrl != null) {
            check(this.nextPageUrl != nextPageUrl) { "Service returned identical next page url" }
            this.nextPageUrl = nextPageUrl
        } else {
            this.nextPageUrl = null
        }
    }
}
