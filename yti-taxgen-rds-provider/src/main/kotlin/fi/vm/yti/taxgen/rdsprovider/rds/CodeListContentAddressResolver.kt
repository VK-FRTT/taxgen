package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps
import okhttp3.HttpUrl

internal data class ContentAddress(
    val codeListUrl: HttpUrl,
    val codesUrl: HttpUrl,
    val extensionUrls: List<ExtensionAddress>
)

internal data class ExtensionAddress(
    val extensionUri: String,
    val extensionUrl: HttpUrl,
    val extensionMembersUrl: HttpUrl
)

internal class CodeListContentAddressResolver(
    codeLisUri: String,
    private val blueprint: CodeListBlueprint,
    private val diagnostic: Diagnostic
) {
    private val codeLisUri = HttpUrl.parse(codeLisUri) ?: diagnostic.fatal("Malformed URI")
    private val passthroughUriParams = resolveUriPassthroughParams()
    val contentAddress = resolveContentAddress()

    fun decorateUriWithInheritedParams(uri: String): String {
        val httpUrlBuilder = HttpUrl.parse(uri)?.newBuilder() ?: diagnostic.fatal("Malformed URI for decoration")

        passthroughUriParams.forEach { (name, values) ->
            values.forEach { value ->
                httpUrlBuilder.setQueryParameter(name, value)
            }
        }

        return httpUrlBuilder.build().toString()
    }

    private fun resolveUriPassthroughParams(): Map<String, List<String>> {
        return arrayOf("env").map { name ->
            name to codeLisUri.queryParameterValues(name)
        }.toMap()
    }

    private fun resolveContentAddress(): ContentAddress {
        val metaDataJson = fetchUriMetaDataJson()

        val httpUrl = metaDataJson.httpUrlAt(
            "/url",
            diagnostic,
            "Content URL resolution via URI"
        )

        val codeListJson = fetchExpandedCodeListJson(httpUrl)

        return ContentAddress(
            codeListUrl = resolveCodeListContentUrl(codeListJson),
            codesUrl = resolveCodesContentUrl(codeListJson),
            extensionUrls = resolveExtensionContentAddress(codeListJson)
        )
    }

    private fun fetchUriMetaDataJson(): JsonNode {
        val uriResolutionData = HttpOps.fetchJsonData(codeLisUri, diagnostic)
        return JsonOps.readTree(uriResolutionData, diagnostic)
    }

    private fun fetchExpandedCodeListJson(url: HttpUrl): JsonNode {
        val expandedUrl = url
            .newBuilder()
            .addQueryParameter("expand", "extension")
            .build()

        val data = HttpOps.fetchJsonData(expandedUrl, diagnostic)
        return JsonOps.readTree(data, diagnostic)
    }

    private fun resolveCodeListContentUrl(codeListJson: JsonNode): HttpUrl {
        return codeListJson
            .httpUrlAt("/url", diagnostic, "CodeList at content URL resolution")
            .newBuilder()
            .addQueryParameter("expand", "code")
            .build()
    }

    private fun resolveCodesContentUrl(codeListJson: JsonNode): HttpUrl {
        return codeListJson.httpUrlAt("/codesUrl", diagnostic, "Codes at content URL resolution")
    }

    private fun resolveExtensionContentAddress(codeListJson: JsonNode): List<ExtensionAddress> {
        return codeListJson.arrayAt("/extensions", diagnostic).mapNotNull { extensionNode ->

            val propertyTypeUri = extensionNode.nonBlankTextAt("/propertyType/uri", diagnostic)

            if (blueprint.extensionPropertyTypeUris.contains(propertyTypeUri)) {
                extensionAddressFromExtensionNode(extensionNode)
            } else {
                null
            }
        }
    }

    private fun extensionAddressFromExtensionNode(extensionNode: JsonNode): ExtensionAddress {
        return ExtensionAddress(
            extensionUri = extensionNode
                .nonBlankTextAt("/uri", diagnostic),
            extensionUrl = extensionNode
                .httpUrlAt("/url", diagnostic, "Extension at content URL resolution"),
            extensionMembersUrl = extensionNode
                .httpUrlAt("/membersUrl", diagnostic, "ExtensionMembers at content URL resolution")
                .newBuilder()
                .addQueryParameter(
                    "expand",
                    "memberValue"
                )
                .build()
        )
    }

    private fun JsonNode.httpUrlAt(jsonPtrExpr: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        val rawUrl = nonBlankTextAt(jsonPtrExpr, diagnostic)
        return parseHttpUrlAt(rawUrl, diagnostic, diagnosticName)
    }

    private fun parseHttpUrlAt(rawUrl: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        return HttpUrl.parse(rawUrl) ?: diagnostic.fatal("Malformed URL ($diagnosticName): $rawUrl")
    }
}