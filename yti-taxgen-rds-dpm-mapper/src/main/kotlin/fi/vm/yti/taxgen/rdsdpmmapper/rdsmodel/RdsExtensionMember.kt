package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic

internal data class RdsExtensionMember(
    //id
    //uri
    //codeValue - Currently not present in of ExtensionMember JSON

    //created
    //modified
    //startDate
    //endDate

    //prefLabel
    //description - Currently not present in of ExtensionMember JSON

    val code: RdsCode?,
    val memberValues: List<RdsMemberValue>?,
    val relatedMember: RdsExtensionMember?
) : RdsEntity() {

    fun validCodeUri(diagnostic: Diagnostic): String {
        if (code == null) diagnostic.fatal("RDS Extension Member not having valid Code element")
        return code.validUri(diagnostic)
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
