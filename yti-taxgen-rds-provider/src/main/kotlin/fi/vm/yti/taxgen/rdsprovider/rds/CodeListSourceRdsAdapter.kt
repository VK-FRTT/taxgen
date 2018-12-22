package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListExtensionSource
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class CodeListSourceRdsAdapter(
    private val rdsCodeListAddress: String,
    private val rdsCodeListAddressType: AddressType,
    private val blueprint: CodeListBlueprint,
    private val diagnostic: Diagnostic
) : CodeListSource, PaginationAwareCollectionIterator.IterationObserver {

    enum class AddressType {
        URI,
        URL
    }

    private val contentUrls: ContentUrls by lazy(this::resolveContentUrls)
    private val subCodeListUrls = mutableListOf<String>()
    private var subCodeListUrlsReady = false

    override fun blueprint(): CodeListBlueprint {
        return blueprint
    }

    override fun codeListMetaData(): String {
        return HttpOps.fetchJsonData(contentUrls.codeListUrl, diagnostic)
    }

    override fun codePagesData(): Sequence<String> {
        return PaginationAwareCollectionIterator(
            contentUrls.codesUrl,
            diagnostic,
            DiagnosticContextType.RdsCodesPage,
            this
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
        check(subCodeListUrlsReady)

        return subCodeListUrls.asSequence().map {
            CodeListSourceRdsAdapter(
                rdsCodeListAddress = it,
                rdsCodeListAddressType = CodeListSourceRdsAdapter.AddressType.URL,
                blueprint = blueprint.subCodeListBlueprint!!,
                diagnostic = diagnostic
            )
        }
    }

    private fun resolveContentUrls(): ContentUrls {
        return diagnostic.withContext(
            contextType = DiagnosticContextType.InitContentUrls,
            contextIdentifier = rdsCodeListAddress
        ) {
            CodeListContentUrlsResolver(blueprint, diagnostic)
                .resolveForAddress(
                    rdsCodeListAddress,
                    rdsCodeListAddressType
                )
        }
    }

    override fun iteratedPage(pageJson: JsonNode) {
        subCodeListUrls.addAll(
            pageJson.arrayAt("/results", diagnostic).mapNotNull { codeNode ->
                codeNode.nonBlankTextOrNullAt("/subCodeScheme/url")
            }
        )
    }

    override fun iterationDone() {
        subCodeListUrlsReady = true
    }
}
