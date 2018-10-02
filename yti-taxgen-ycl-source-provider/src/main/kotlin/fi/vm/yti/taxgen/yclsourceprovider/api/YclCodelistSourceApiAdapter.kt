package fi.vm.yti.taxgen.yclsourceprovider.api

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistExtensionSource
import fi.vm.yti.taxgen.yclsourceprovider.YclCodelistSource
import fi.vm.yti.taxgen.yclsourceprovider.config.YclCodelistSourceConfig
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps

internal class YclCodelistSourceApiAdapter(
    private val config: YclCodelistSourceConfig,
    private val diagnostic: Diagnostic
) : YclCodelistSource {

    private val contentUrls: ContentUrls by lazy(this::resolveContentUrls)

    override fun yclCodelistSourceConfigData(): String = JsonOps.writeAsJsonString(config)

    override fun yclCodeSchemeData(): String {
        return HttpOps.fetchJsonData(contentUrls.codeSchemeUrl, diagnostic)
    }

    override fun yclCodePagesData(): Sequence<String> {
        return YclPaginationAwareResourceIterator(
            contentUrls.codesUrl,
            diagnostic,
            DiagnosticContextType.YclCodesPage
        ).asSequence()
    }

    override fun yclCodelistExtensionSources(): List<YclCodelistExtensionSource> {
        return contentUrls.extensionUrls.map { extensionUrls ->
            YclCodelistExtensionSourceApiAdapter(
                extensionUrls,
                diagnostic
            )
        }
    }

    private fun resolveContentUrls(): ContentUrls {
        return diagnostic.withContext(
            contextType = DiagnosticContextType.InitUriResolution,
            contextIdentifier = config.uri
        ) {
            YclCodelistContentUrlsResolver(diagnostic).resolveContentUrlsFromUri(config.uri)
        }
    }
}
