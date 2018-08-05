package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import java.time.Instant
import java.time.LocalDate

internal data class YclCodeScheme(
    val created: Instant?,
    val modified: Instant?,

    val codeValue: String?,
    val defaultCode: YclDefaultCode?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val prefLabel: MutableMap<String, String>?,
    val definition: MutableMap<String, String>?,
    val description: MutableMap<String, String>?
)
