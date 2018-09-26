package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.time.Instant
import java.time.LocalDate

internal data class YclCode(
    val id: String?,

    val created: Instant?,
    val modified: Instant?,

    val codeValue: String?,

    val startDate: LocalDate?,
    val endDate: LocalDate?,

    val order: Int?,

    val prefLabel: Map<String, String>?,
    val description: Map<String, String>?
) : DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCode
    override fun contextName(): String = prefLabel?.get("en") ?: ""
    override fun contextRef(): String = codeValue ?: ""
}
