package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.time.Instant
import java.time.LocalDate

internal data class YclCode(
    override val id: String?,
    override val uri: String?,
    override val codeValue: String?,

    override val created: Instant?,
    override val modified: Instant?,
    override val startDate: LocalDate?,
    override val endDate: LocalDate?,

    override val prefLabel: Map<String, String>?,
    override val description: Map<String, String>?
) : YclEntity(), DiagnosticContextProvider {

    override fun contextType(): DiagnosticContextType = DiagnosticContextType.YclCode
    override fun contextName(): String = composeContextName()
    override fun contextRef(): String = composeContextRef()

    fun asMemberCode(memberCodePrefix: String?): String {
        return "${memberCodePrefix ?: ""}${codeValue ?: ""}"
    }

    fun isDefaultCode(defaultCode: YclCode?): Boolean {
        if (defaultCode == null) {
            return false
        }

        return idOrEmpty() == defaultCode.idOrEmpty()
    }
}
