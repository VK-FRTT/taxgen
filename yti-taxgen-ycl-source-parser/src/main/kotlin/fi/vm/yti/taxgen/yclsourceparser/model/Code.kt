package fi.vm.yti.taxgen.yclsourceparser.model

import java.time.Instant
import java.time.LocalDate
import java.util.*

data class Code(
    val id: UUID?,

    val modified: Instant?,

    val codeValue: String?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val broaderCodeId: UUID?,
    val order: Int?,
    val prefLabel: Map<String, String>?,
    val description: Map<String, String>?,
    val definition: Map<String, String>?
)
