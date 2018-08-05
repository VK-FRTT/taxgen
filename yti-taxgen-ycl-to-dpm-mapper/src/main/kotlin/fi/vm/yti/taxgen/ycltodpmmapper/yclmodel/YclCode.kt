package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import java.time.Instant
import java.time.LocalDate

internal data class YclCode(
    val created: Instant?,
    val modified: Instant?,

    val codeValue: String?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val order: Int?,

    val prefLabel: Map<String, String>?,
    val description: Map<String, String>?
) : DiagnosticTopicProvider {

    override fun topicType(): String = "YCL Code"
    override fun topicName(): String = prefLabel?.get("en") ?: ""
    override fun topicIdentifier(): String = codeValue ?: ""
}
