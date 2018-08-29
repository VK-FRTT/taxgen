package fi.vm.yti.taxgen.commons

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic

object JsonOps {

    val lenientObjectMapper by lazy(this::createLenientObjectMapper)

    fun readTree(content: String, diagnostic: Diagnostic): JsonNode {
        return try {
            lenientObjectMapper.readTree(content)
        } catch (e: JsonProcessingException) {
            diagnostic.fatal("Processing JSON content tree failed: ${e.message}")
        }
    }

    inline fun <reified T : Any> readValue(content: String, diagnostic: Diagnostic): T {
        return try {
            lenientObjectMapper.readValue(content)
        } catch (e: JsonProcessingException) {
            diagnostic.fatal("Processing JSON content failed: ${e.message}")
        }
    }

    fun writeAsJsonString(value: Any): String {
        return lenientObjectMapper.writeValueAsString(value)
    }

    private fun createLenientObjectMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        mapper.registerModule(JavaTimeModule())

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        return mapper
    }
}
