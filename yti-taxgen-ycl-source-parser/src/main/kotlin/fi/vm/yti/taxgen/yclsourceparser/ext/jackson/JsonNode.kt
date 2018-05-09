package fi.vm.yti.taxgen.yclsourceparser.ext.jackson

import com.fasterxml.jackson.databind.JsonNode

fun JsonNode.nonBlankTextOrNullAt(jsonPtrExpr: String): String? {
    val node = at(jsonPtrExpr) ?: return null
    val text = node.textValue()
    if (text.isNullOrBlank()) return null
    return text
}
