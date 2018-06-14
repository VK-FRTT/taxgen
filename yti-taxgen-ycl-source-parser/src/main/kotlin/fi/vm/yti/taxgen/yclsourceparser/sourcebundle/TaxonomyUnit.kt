package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

interface TaxonomyUnit {
    fun taxonomyUnitInfoData(): String
    fun codeLists(): List<CodeList>
}
