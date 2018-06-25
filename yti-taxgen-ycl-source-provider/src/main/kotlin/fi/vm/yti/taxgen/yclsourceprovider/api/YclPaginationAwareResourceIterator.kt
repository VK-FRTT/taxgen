package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JacksonObjectMapper
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps

class YclPaginationAwareResourceIterator(
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
        val responseJson =
            JacksonObjectMapper.lenientObjectMapper().readTree(response)
                ?: throw YclCodelistSourceApiAdapter.InitFailException()

        val nextPageUrl = responseJson.nonBlankTextOrNullAt("/meta/nextPage")
        check(this.nextPageUrl != nextPageUrl) { "Service returned identical next page url" }
        this.nextPageUrl = nextPageUrl
    }
}
