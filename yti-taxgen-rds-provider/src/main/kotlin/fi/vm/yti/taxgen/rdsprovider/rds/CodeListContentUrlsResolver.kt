package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps
import okhttp3.HttpUrl

internal data class ContentUrls(
    val codeListUrl: HttpUrl,
    val codesUrl: HttpUrl,
    val extensionUrls: List<ExtensionUrls>
)

internal data class ExtensionUrls(
    val extensionUrl: HttpUrl,
    val extensionMembersUrl: HttpUrl
)

internal class CodeListContentUrlsResolver(
    private val blueprint: CodeListBlueprint,
    private val diagnostic: Diagnostic
) {
    fun resolveForUri(
        uri: String
    ): ContentUrls {

        val metaDataJson = fetchUriMetaDataJson(uri)
        val httpUrl = metaDataJson.httpUrlAt(
            "/url",
            diagnostic,
            "Content URL resolution via URI"
        )

        val codeListJson = fetchExpandedCodeListJson(httpUrl)

        return ContentUrls(
            codeListUrl = resolveCodeListContentUrl(codeListJson),
            codesUrl = resolveCodesContentUrl(codeListJson),
            extensionUrls = resolveKnownExtensionContentUrls(codeListJson)
        )
    }

    private fun fetchUriMetaDataJson(uri: String): JsonNode {
        val httpUrl = HttpUrl.parse(uri) ?: diagnostic.fatal("Malformed URI")
        val uriResolutionData = HttpOps.fetchJsonData(httpUrl, diagnostic)
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

    private fun resolveKnownExtensionContentUrls(codeListJson: JsonNode): List<ExtensionUrls> {
        return codeListJson.arrayAt("/extensions", diagnostic).mapNotNull { extensionNode ->

            val propertyTypeUri = extensionNode.nonBlankTextAt("/propertyType/uri", diagnostic)

            if (blueprint.extensionPropertyTypeUris.contains(propertyTypeUri)) {
                extensionUrlsFromExtensionNode(extensionNode)
            } else {
                null
            }
        }
    }

    private fun extensionUrlsFromExtensionNode(extensionNode: JsonNode): ExtensionUrls {
        return ExtensionUrls(
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
