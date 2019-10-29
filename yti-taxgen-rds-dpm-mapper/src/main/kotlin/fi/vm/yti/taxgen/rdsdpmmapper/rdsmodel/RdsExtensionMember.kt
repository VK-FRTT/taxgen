package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic

internal data class RdsExtensionMember(
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
        return validCode(diagnostic).validUri(diagnostic)
    }

    fun stringValueOrEmpty(valueType: RdsMemberValueType): String {
        return memberValueOfType(valueType.typeUri)?.value ?: ""
    }

    fun nonEmptyStringValueOrNull(valueType: RdsMemberValueType): String? {
        val value = memberValueOfType(valueType.typeUri)?.value ?: return null

        if (value.isEmpty()) {
            return null
        }

        return value
    }

    private fun memberValueOfType(typeUri: String): RdsMemberValue? {
        return memberValues?.find { it.valueType?.uri == typeUri }
    }

    private fun validCode(diagnostic: Diagnostic): RdsCode {
        return code ?: diagnostic.fatal("RDS Extension Member not having valid Code element")
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
