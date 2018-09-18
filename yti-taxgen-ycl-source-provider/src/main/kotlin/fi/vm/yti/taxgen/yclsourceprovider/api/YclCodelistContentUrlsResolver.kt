package fi.vm.yti.taxgen.yclsourceprovider.api

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.yclsourceprovider.helpers.HttpOps
import okhttp3.HttpUrl

internal data class ContentUrls(
    val codeSchemeUrl: HttpUrl,
    val codesUrl: HttpUrl,
    val extensionUrls: List<ExtensionUrls>
)

internal data class ExtensionUrls(
    val extensionUrl: HttpUrl,
    val extensionMembersUrl: HttpUrl
)

internal class YclCodelistContentUrlsResolver(
    val diagnostic: Diagnostic
) {
    fun resolveContentUrlsFromUri(uri: String): ContentUrls {
        val uriResolutionJson = fetchUriResponseJson(uri)
        val codeSchemeJson = fetchExpandedCodeSchemeJson(uriResolutionJson)

        return ContentUrls(
            codeSchemeUrl = resolveCodeSchemeContentUrl(codeSchemeJson),
            codesUrl = resolveCodesContentUrl(codeSchemeJson),
            extensionUrls = resolveExtensionSchemeContentUrls(codeSchemeJson)
        )
    }

    private fun fetchUriResponseJson(uri: String): JsonNode {
        val httpUrl = HttpUrl.parse(uri) ?: diagnostic.fatal("Malformed URI")
        val uriResolutionData = HttpOps.fetchJsonData(httpUrl, diagnostic)
        return JsonOps.readTree(uriResolutionData, diagnostic)
    }

    private fun fetchExpandedCodeSchemeJson(uriResolutionJson: JsonNode): JsonNode {
        val expandedUrl = uriResolutionJson
            .httpUrlAt("/url", diagnostic, "code scheme at URI resolution")
            .newBuilder()
            .addQueryParameter("expand", "extensionScheme,propertyType")
            .build()

        val data = HttpOps.fetchJsonData(expandedUrl, diagnostic)
        return JsonOps.readTree(data, diagnostic)
    }

    private fun resolveCodeSchemeContentUrl(codeSchemeJson: JsonNode): HttpUrl {
        return codeSchemeJson
            .httpUrlAt("/url", diagnostic, "code list")
            .newBuilder()
            .addQueryParameter("expand", "code")
            .build()
    }

    private fun resolveCodesContentUrl(codeSchemeJson: JsonNode): HttpUrl {
        return codeSchemeJson.httpUrlAt("/codesUrl", diagnostic, "codes")
    }

    private fun resolveExtensionSchemeContentUrls(codeSchemeJson: JsonNode): List<ExtensionUrls> {
        return codeSchemeJson.arrayAt("/extensionSchemes", diagnostic).map { item ->
            ExtensionUrls(
                extensionUrl = item
                    .httpUrlAt("/url", diagnostic, "extension")
                    .newBuilder()
                    .addQueryParameter("expand", "propertyType")
                    .build(),
                extensionMembersUrl = item
                    .httpUrlAt("/extensionsUrl", diagnostic, "extension members")
            )
        }
    }

    private fun JsonNode.httpUrlAt(jsonPtrExpr: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        val rawUrl = nonBlankTextAt(jsonPtrExpr, diagnostic)
        return HttpUrl.parse(rawUrl) ?: diagnostic.fatal("Malformed URL ($diagnosticName): $rawUrl")
    }
}
