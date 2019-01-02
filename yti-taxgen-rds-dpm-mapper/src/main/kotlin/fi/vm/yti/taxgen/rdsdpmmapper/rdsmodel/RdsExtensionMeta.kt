package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

import java.time.Instant
import java.time.LocalDate

internal data class RdsExtensionMeta(
    override val id: String?,
    override val uri: String?,
    override val codeValue: String?,

    override val created: Instant?,
    override val modified: Instant?,

    override val startDate: LocalDate?,
    override val endDate: LocalDate?,

    override val prefLabel: Map<String, String>?,
    override val description: Map<String, String>?, //Currently not present in of Extension JSON

    val propertyType: RdsPropertyType?
) : RdsEntity() {

    fun isType(type: RdsExtensionType): Boolean {
        return (propertyType?.uri == type.typeUri)
    }
}

enum class RdsExtensionType(val typeUri: String) {
    DpmMetric("http://uri.suomi.fi/datamodel/ns/code#dpmMetric"),
    DpmExplicitDomain("http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain"),
    DefinitionHierarchy("http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy"),
    CalculationHierarchy("http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy")
}
