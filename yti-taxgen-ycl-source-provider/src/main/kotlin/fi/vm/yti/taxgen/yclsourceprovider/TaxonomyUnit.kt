package fi.vm.yti.taxgen.yclsourceprovider

interface TaxonomyUnit {
    fun taxonomyUnitInfoData(): String
    fun codeLists(): List<CodeList>
}
