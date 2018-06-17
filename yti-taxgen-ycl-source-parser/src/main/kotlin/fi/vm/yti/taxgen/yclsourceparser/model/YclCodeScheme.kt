package fi.vm.yti.taxgen.yclsourceparser.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class YclCodeScheme(
    val id: UUID?,

    val created: Instant?,
    val modified: Instant?,

    val codeValue: String?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val prefLabel: MutableMap<String, String>,
    val definition: MutableMap<String, String>,
    val description: MutableMap<String, String>
)
