package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

interface TaxonomyUnit {
    fun taxonomyUnitDescriptor(): String
    fun codeLists(): List<CodeList>
}
