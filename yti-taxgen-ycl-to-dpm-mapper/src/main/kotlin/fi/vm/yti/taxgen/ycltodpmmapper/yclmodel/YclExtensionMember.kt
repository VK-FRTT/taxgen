package fi.vm.yti.taxgen.ycltodpmmapper.yclmodel

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.datapointmetamodel.DpmElementRef
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.dpmElementRef
import java.time.Instant
import java.time.LocalDate

internal data class YclExtensionMember(
    override val id: String?,
    override val uri: String?,
    override val codeValue: String?, //Currently not present in of ExtensionMember JSON

    override val created: Instant?,
    override val modified: Instant?,
    override val startDate: LocalDate?,
    override val endDate: LocalDate?,

    override val prefLabel: Map<String, String>?,
    override val description: Map<String, String>?, //Currently not present in of ExtensionMember JSON

    val code: YclCode?,
    val memberValues: List<YclMemberValue>?,
    val relatedMember: YclExtensionMember?
) : YclEntity(), DiagnosticContextProvider {
    override fun contextType() = DiagnosticContextType.YclExtensionMember
    override fun contextLabel() = diagnosticLabel()
    override fun contextIdentifier() = diagnosticIdentifier()

    fun isRootMember() = relatedMember == null

    fun isChildOf(other: YclExtensionMember): Boolean {
        if (relatedMember?.uri == null) return false
        if (other.uri == null) return false

        return relatedMember.uri == other.uri
    }

    fun comparisonOpOrNull(): String? {
        return memberValueOfType("http://uri.suomi.fi/datamodel/ns/code#comparisonOperator")?.value
    }

    fun unaryOpOrNull(): String? {
        return memberValueOfType("http://uri.suomi.fi/datamodel/ns/code#unaryOperator")?.value
    }

    fun memberRef(): DpmElementRef { //TODO - test code reference null case
        return if (code != null) {
            dpmElementRef<Member>(code.idOrEmpty(), code.uriOrEmpty(), code.diagnosticLabel())
        } else {
            dpmElementRef<Member>("", "", "${diagnosticLabel()}: No Code reference")
        }
    }

    private fun memberValueOfType(typeUri: String): YclMemberValue? {
        return memberValues?.find { it.valueType?.uri == typeUri }
    }
}
