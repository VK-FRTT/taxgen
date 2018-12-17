package fi.vm.yti.taxgen.rdsprovider.rds

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.ext.jackson.arrayAt
import fi.vm.yti.taxgen.commons.ext.jackson.nonBlankTextAt
import fi.vm.yti.taxgen.rdsprovider.helpers.HttpOps
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

internal class RdsContentUrlsResolver(
    val diagnostic: Diagnostic
) {
    fun runWithUri(uri: String): ContentUrls {
        val metadataJson = fetchUriMetadataJson(uri)
        val codeSchemeJson = fetchExpandedCodeSchemeJson(metadataJson)

        return ContentUrls(
            codeSchemeUrl = resolveCodeSchemeContentUrl(codeSchemeJson),
            codesUrl = resolveCodesContentUrl(codeSchemeJson),
            extensionUrls = resolveExtensionContentUrls(codeSchemeJson)
        )
    }

    private fun fetchUriMetadataJson(uri: String): JsonNode {
        val httpUrl = HttpUrl.parse(uri) ?: diagnostic.fatal("Malformed URI")
        val uriResolutionData = HttpOps.fetchJsonData(httpUrl, diagnostic)
        return JsonOps.readTree(uriResolutionData, diagnostic)
    }

    private fun fetchExpandedCodeSchemeJson(metadataJson: JsonNode): JsonNode {
        val expandedUrl = metadataJson
            .httpUrlAt("/url", diagnostic, "code scheme at URI resolution")
            .newBuilder()
            .addQueryParameter("expand", "extension")
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

    private fun resolveExtensionContentUrls(codeSchemeJson: JsonNode): List<ExtensionUrls> {
        return codeSchemeJson.arrayAt("/extensions", diagnostic).map { item ->
            ExtensionUrls(
                extensionUrl = item
                    .httpUrlAt("/url", diagnostic, "extension"),
                extensionMembersUrl = item
                    .httpUrlAt("/membersUrl", diagnostic, "extension members")
                    .newBuilder()
                    .addQueryParameter("expand", "memberValue,code") //TODO - code can be removed when member are referred via URI
                    .build()
            )
        }
    }

    private fun JsonNode.httpUrlAt(jsonPtrExpr: String, diagnostic: Diagnostic, diagnosticName: String): HttpUrl {
        val rawUrl = nonBlankTextAt(jsonPtrExpr, diagnostic)
        return HttpUrl.parse(rawUrl) ?: diagnostic.fatal("Malformed URL ($diagnosticName): $rawUrl")
    }
}
