package fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel

internal data class RdsExtensionMeta(
    //id
    //uri
    //codeValue

    //created
    //modified

    //startDate
    //endDate

    //prefLabel
    //description - Currently not present in of Extension JSON

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
