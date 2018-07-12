package fi.vm.yti.taxgen.commons

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JacksonObjectMapper {

    private val lenientObjectMapper = createLenientObjectMapper()

    private fun createLenientObjectMapper(): ObjectMapper {
        val mapper = jacksonObjectMapper()
        mapper.registerModule(JavaTimeModule())

        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        return mapper
    }

    fun lenientObjectMapper(): ObjectMapper = lenientObjectMapper //TODO - minimise usage by passing via ctx
}
