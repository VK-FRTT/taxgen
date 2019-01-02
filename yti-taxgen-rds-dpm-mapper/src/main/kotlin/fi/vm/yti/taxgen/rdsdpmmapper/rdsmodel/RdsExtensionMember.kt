package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import java.time.Instant
import java.time.LocalDate

internal data class RdsExtensionMember(
    override val id: String?,
    override val uri: String?,
    override val codeValue: String?, //Currently not present in of ExtensionMember JSON

    override val created: Instant?,
    override val modified: Instant?,
    override val startDate: LocalDate?,
    override val endDate: LocalDate?,

    override val prefLabel: Map<String, String>?,
    override val description: Map<String, String>?, //Currently not present in of ExtensionMember JSON

    val code: RdsCode?,
    val memberValues: List<RdsMemberValue>?,
    val relatedMember: RdsExtensionMember?
) : RdsEntity(), DiagnosticContextProvider {

    override fun contextType() = DiagnosticContextType.RdsExtensionMember
    override fun contextLabel() = diagnosticLabel()
    override fun contextIdentifier() = diagnosticIdentifier()

    fun validCodeUri(diagnostic: Diagnostic): String {
        if (code == null) diagnostic.fatal("RDS Extension Member not having valid Code element")
        return code.validUri(diagnostic)
    }

    fun isRootMember() = relatedMember == null

    fun isChildOf(other: RdsExtensionMember): Boolean {
        if (relatedMember?.uri == null) return false
        if (other.uri == null) return false

        return relatedMember.uri == other.uri
    }

    fun stringValueOrEmpty(valueType: RdsMemberValueType): String {
        return memberValueOfType(valueType.typeUri)?.value ?: ""
    }

    fun stringValueOrNull(valueType: RdsMemberValueType): String? {
        return memberValueOfType(valueType.typeUri)?.value
    }

    private fun memberValueOfType(typeUri: String): RdsMemberValue? {
        return memberValues?.find { it.valueType?.uri == typeUri }
    }
}

enum class RdsMemberValueType(val typeUri: String) {
    UnaryOperator("http://uri.suomi.fi/datamodel/ns/code#unaryOperator"),
    ComparisonOperator("http://uri.suomi.fi/datamodel/ns/code#comparisonOperator"),
    DpmMetricDataType("http://uri.suomi.fi/datamodel/ns/code#dpmMetricDataType"),
    DpmDomainDataType("http://uri.suomi.fi/datamodel/ns/code#dpmDomainDataType"),
    DpmFlowType("http://uri.suomi.fi/datamodel/ns/code#dpmFlowType"),
    DpmBalanceType("http://uri.suomi.fi/datamodel/ns/code#dpmBalanceType"),
    DpmDomainReference("http://uri.suomi.fi/datamodel/ns/code#dpmDomainReference"),
    DpmHierarchyReference("http://uri.suomi.fi/datamodel/ns/code#dpmHierarchyReference"),
    DpmMemberXBRLCodePrefix("http://uri.suomi.fi/datamodel/ns/code#dpmMemberXBRLCodePrefix")
}
