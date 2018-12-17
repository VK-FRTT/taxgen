package fi.vm.yti.taxgen.rdsprovider.rds

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class CodeListSourceRdsAdapter(
    private val diagnostic: Diagnostic,
    private val rdsCodelistUri: String
) : CodeListSource {

    private val contentUrls: ContentUrls by lazy(this::resolveContentUrls)

    override fun codeListData(): String {
        return HttpOps.fetchJsonData(contentUrls.codeSchemeUrl, diagnostic)
    }

    override fun codePagesData(): Sequence<String> {
        return RdsPaginationAwareResourceIterator(
            contentUrls.codesUrl,
            diagnostic,
            DiagnosticContextType.RdsCodesPage
        ).asSequence()
    }

    override fun extensionSources(): Sequence<CodeListExtensionSource> {
        return contentUrls.extensionUrls.map { extensionUrls ->
            CodeListExtensionSourceAdapter(
                extensionUrls,
                diagnostic
            )
        }.asSequence()
    }

    override fun subCodeListSources(): Sequence<CodeListSource> {
        return emptySequence()
    }

    private fun resolveContentUrls(): ContentUrls {
        return diagnostic.withContext(
            contextType = DiagnosticContextType.InitUriResolution,
            contextIdentifier = rdsCodelistUri
        ) {
            RdsContentUrlsResolver(diagnostic).runWithUri(rdsCodelistUri)
        }
    }
}
