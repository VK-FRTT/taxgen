package fi.vm.yti.taxgen.ycltodpmmapper

import com.fasterxml.jackson.databind.ObjectMapper
import fi.vm.yti.taxgen.datapointmetamodel.Owner

data class DpmMappingContext(
    val owner: Owner,
    val objectMapper: ObjectMapper
)
