package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.FileOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.helpers.HttpOps
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.CodeListConfig

class YclServiceCodeList(
    private val codeListConfig: CodeListConfig
) : CodeList {

    private var resolvedUrlsCache: ResolvedUrls? = null

    data class ResolvedUrls(
        val codeListData: String,
        val codesUrl: String
    )

    override fun codeList(): String {
        val urls = resolvedUrls()
        return urls.codeListData
    }

    override fun codesPages(): Iterator<String> {
        val urls = resolvedUrls()
        return YclPagedResourceRetriever(urls.codesUrl)
    }

    private fun resolvedUrls(): ResolvedUrls {
        return resolvedUrlsCache ?: resolveUrls().also { resolvedUrlsCache = it }
    }

    private fun resolveUrls(): ResolvedUrls {
        val codeListData = HttpOps.getJsonData(codeListConfig.uri)
        val codeListJson = FileOps.lenientObjectMapper().readTree(codeListData) ?: throw InitFailException()
        val codesUrl = codeListJson.nonBlankTextOrNullAt("/codesUrl") ?: throw InitFailException()

        return ResolvedUrls(
            codeListData = codeListData,
            codesUrl = codesUrl
        )
    }

    class InitFailException : RuntimeException()
}
