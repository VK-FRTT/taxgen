package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class CodeListSourceRdsAdapter(
    private val rdsCodeListUri: String,
    private val blueprint: CodeListBlueprint,
    private val diagnostic: Diagnostic
) : CodeListSource {

    private val contentAddress: ContentAddress by lazy(this::resolveContentAddress)
    private var subCodeListUris: List<String>? = null

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = rdsCodeListUri

    override fun blueprint(): CodeListBlueprint = blueprint

    override fun codeListMetaData(): String {
        return HttpOps.fetchJsonData(contentAddress.codeListUrl, diagnostic)
    }

    override fun codePagesData(): Sequence<String> {
        return PaginationAwareCollectionIterator(
            contentAddress.codesUrl,
            diagnostic,
            DiagnosticContextType.RdsCodesPage,
            SubCodeListUriExtractor(this)
        ).asSequence()
    }

    override fun extensionSources(): Sequence<ExtensionSource> {
        return contentAddress.extensionUrls.map { extensionUrls ->
            ExtensionSourceRdsAdapter(
                extensionUrls,
                diagnostic
            )
        }.asSequence()
    }

    override fun subCodeListSources(): Sequence<CodeListSource> {
        if (subCodeListUris == null) {
            codePagesData().all { true }
        }

        subCodeListUris?.let { uris ->
            return uris.asSequence().map {
                CodeListSourceRdsAdapter(
                    rdsCodeListUri = it,
                    blueprint = blueprint.subCodeListBlueprint!!,
                    diagnostic = diagnostic
                )
            }
        }

        thisShouldNeverHappen("subCodeListUris is null")
    }

    private fun resolveContentAddress(): ContentAddress {
        return (diagnostic as DiagnosticContext).withContext(
            contextType = DiagnosticContextType.InitContentAddress,
            contextIdentifier = rdsCodeListUri
        ) {
            val resolver = CodeListContentAddressResolver(blueprint, diagnostic)

            resolver.resolveForUri(rdsCodeListUri)
        }
    }

    private class SubCodeListUriExtractor(
        val parentAdapter: CodeListSourceRdsAdapter
    ) : PaginationAwareCollectionIterator.IterationObserver {

        private val subCodeListUris = mutableListOf<String>()

        override fun iteratedPage(pageJson: JsonNode) {
            subCodeListUris.addAll(
                pageJson.arrayAt("/results", parentAdapter.diagnostic).mapNotNull { codeNode ->
                    codeNode.nonBlankTextOrNullAt("/subCodeScheme/uri")
                }
            )
        }

        override fun iterationDone() {
            parentAdapter.subCodeListUris = subCodeListUris.distinct()
        }
    }
}
