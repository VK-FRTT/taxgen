package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

class FixedSourceBundle : SourceBundle {
    override fun bundleInfoData() = """{"marker": "fixed_source_bundle"}"""
    override fun taxonomyUnits() = listOf(FixedTaxonomyUnit(0), FixedTaxonomyUnit(1))
    override fun close() {}
}

class FixedTaxonomyUnit(private val index: Int) : TaxonomyUnit {
    override fun taxonomyUnitInfoData() = """{"marker": "fixed_taxonomyunit_$index"}"""
    override fun codeLists() = listOf(FixedCodeList(0), FixedCodeList(1))
}

class FixedCodeList(private val index: Int) : CodeList {
    override fun codeListData() = """{"marker": "fixed_codelist_$index"}"""
    override fun codePagesData() = listOf(
        """{"marker": "fixed_codepage_0"}""",
        """{"marker": "fixed_codepage_1"}"""
    ).iterator()
}
