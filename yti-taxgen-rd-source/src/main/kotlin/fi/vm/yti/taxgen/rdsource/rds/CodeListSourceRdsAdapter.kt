package fi.vm.yti.taxgen.rdsource.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextOrNullAt
import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextDetailsData
import fi.vm.yti.taxgen.rdsource.CodeListBlueprint
import fi.vm.yti.taxgen.rdsource.CodeListSource
import fi.vm.yti.taxgen.rdsource.ExtensionSource

internal class CodeListSourceRdsAdapter(
    private val codeListUri: String,
    private val blueprint: CodeListBlueprint,
    private val rdsClient: RdsClient,
    private val diagnosticContext: DiagnosticContext
) : CodeListSource {

    private val contentAddressResolver: CodeListContentAddressResolver by lazy(this::resolveContentAddress)
    private var subCodeListUris: List<String>? = null

    override fun contextTitle(): String = ""
    override fun contextIdentifier(): String = codeListUri

    override fun blueprint(): CodeListBlueprint = blueprint

    override fun codeListMetaData(): String {
        return rdsClient.fetchJsonAsString(
            contentAddressResolver.contentAddress.codeListUrl
        )
    }

    override fun eachCodePageData(action: (String) -> Unit) {
        PaginationAwareCollectionIterator(
            contentAddressResolver.contentAddress.codesUrl,
            rdsClient,
            diagnosticContext,
            SubCodeListUriExtractor(this)
        ).forEach(action)
    }

    override fun eachExtensionSource(action: (ExtensionSource) -> Unit) {
        contentAddressResolver.contentAddress.extensionUrls.forEach { extensionUrls ->
            val extensionSource = ExtensionSourceRdsAdapter(
                extensionUrls,
                rdsClient,
                diagnosticContext
            )
            action(extensionSource)
        }
    }

    override fun eachSubCodeListSource(action: (CodeListSource) -> Unit) {
        blueprint.subCodeListBlueprint ?: return

        if (subCodeListUris == null) {
            eachCodePageData {}
        }

        subCodeListUris?.forEach { uri ->
            val codelistSource = CodeListSourceRdsAdapter(
                codeListUri = contentAddressResolver.decorateUriWithInheritedParams(uri),
                blueprint = blueprint.subCodeListBlueprint,
                rdsClient = rdsClient,
                diagnosticContext = diagnosticContext
            )

            action(codelistSource)
        }
    }

    private fun resolveContentAddress(): CodeListContentAddressResolver {
        return diagnosticContext.withContext(
            contextType = DiagnosticContexts.InitContentAddress.toType(),
            contextDetails = DiagnosticContextDetailsData.withContextIdentifier(codeListUri)
        ) {
            CodeListContentAddressResolver(
                codeLisUri = codeListUri,
                blueprint = blueprint,
                rdsClient = rdsClient,
                diagnostic = diagnosticContext
            )
        }
    }

    private class SubCodeListUriExtractor(
        val parentAdapter: CodeListSourceRdsAdapter
    ) : PaginationAwareCollectionIterator.IterationObserver {

        private val subCodeListUris = mutableListOf<String>()

        override fun iteratedPage(pageJson: JsonNode) {
            subCodeListUris.addAll(
                pageJson
                    .arrayAt("/results", parentAdapter.diagnosticContext)
                    .mapNotNull { codeNode ->
                        codeNode.nonBlankTextOrNullAt("/subCodeScheme/uri")
                    }
            )
        }

        override fun iterationDone() {
            parentAdapter.subCodeListUris =
                subCodeListUris
                    .distinct()
                    .sortedWith(
                        compareBy(NumberAwareStringComparator.instance()) { it }
                    )
        }
    }
}
