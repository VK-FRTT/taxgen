package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps

internal class CodeListSourceRdsAdapter(
    private val codeListUri: String,
    private val blueprint: CodeListBlueprint,
    private val diagnostic: Diagnostic
) : CodeListSource {

    private val contentAddressResolver: CodeListContentAddressResolver by lazy(this::resolveContentAddress)
    private var subCodeListUris: List<String>? = null

    override fun contextLabel(): String = ""
    override fun contextIdentifier(): String = codeListUri

    override fun blueprint(): CodeListBlueprint = blueprint

    override fun codeListMetaData(): String {
        return HttpOps.fetchJsonData(
            contentAddressResolver.contentAddress.codeListUrl,
            diagnostic
        )
    }

    override fun eachCodePageData(action: (String) -> Unit) {
        PaginationAwareCollectionIterator(
            contentAddressResolver.contentAddress.codesUrl,
            diagnostic,
            SubCodeListUriExtractor(this)
        ).forEach(action)
    }

    override fun eachExtensionSource(action: (ExtensionSource) -> Unit) {
        contentAddressResolver.contentAddress.extensionUrls.forEach { extensionUrls ->
            val extensionSource = ExtensionSourceRdsAdapter(
                extensionUrls,
                diagnostic
            )
            action(extensionSource)
        }
    }

    override fun eachSubCodeListSource(action: (CodeListSource) -> Unit) {
        if (subCodeListUris == null) {
            eachCodePageData {}
        }

        subCodeListUris?.forEach { uri ->
            val codelistSource = CodeListSourceRdsAdapter(
                codeListUri = contentAddressResolver.decorateUriWithInheritedParams(uri),
                blueprint = blueprint.subCodeListBlueprint!!,
                diagnostic = diagnostic
            )

            action(codelistSource)
        }
    }

    private fun resolveContentAddress(): CodeListContentAddressResolver {
        return (diagnostic as DiagnosticContext).withContext(
            contextType = DiagnosticContextType.InitContentAddress,
            contextIdentifier = codeListUri
        ) {
            CodeListContentAddressResolver(
                codeLisUri = codeListUri,
                blueprint = blueprint,
                diagnostic = diagnostic
            )
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
