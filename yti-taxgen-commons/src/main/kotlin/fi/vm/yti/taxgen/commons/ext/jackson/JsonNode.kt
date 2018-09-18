package fi.vm.yti.taxgen.commons.ext.jackson

import com.fasterxml.jackson.databind.JsonNode
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic

fun JsonNode.nonBlankTextOrNullAt(jsonPtrExpr: String): String? {
    val node = at(jsonPtrExpr) ?: return null
    val text = node.textValue()
    if (text.isNullOrBlank()) return null
    return text
}

fun JsonNode.nonBlankTextAt(jsonPtrExpr: String, diagnostic: Diagnostic): String {
    val text = nonBlankTextOrNullAt(jsonPtrExpr)
    return text ?: diagnostic.fatal("Malformed JSON: no text at '$jsonPtrExpr'")
}

fun JsonNode.arrayOrNullAt(jsonPtrExpr: String): JsonNode? {
    val node = at(jsonPtrExpr) ?: return null
    if (!node.isArray) return null
    return node
}

fun JsonNode.arrayAt(jsonPtrExpr: String, diagnostic: Diagnostic): JsonNode {
    val node = arrayOrNullAt(jsonPtrExpr)
    return node ?: diagnostic.fatal("Malformed JSON: no array at '$jsonPtrExpr'")
}
