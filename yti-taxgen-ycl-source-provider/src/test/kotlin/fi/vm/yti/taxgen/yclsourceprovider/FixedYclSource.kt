package fi.vm.yti.taxgen.yclsourceprovider

class FixedYclSource : YclSource {
    override fun sourceInfoData() = """{"marker": "fixed_source_info"}"""
    override fun dpmDictionarySources() = listOf(FixedDpmDictionarySource(0), FixedDpmDictionarySource(1))
    override fun close() {}
}

class FixedDpmDictionarySource(private val index: Int) : DpmDictionarySource {
    override fun dpmOwnerConfigData() = """{"marker": "fixed_dpm_owner_config_$index"}"""
    override fun yclCodelistSources() = listOf(FixedYclCodelistSource(0), FixedYclCodelistSource(1))
}

class FixedYclCodelistSource(private val index: Int) : YclCodelistSource {
    override fun yclCodeschemeData() = """{"marker": "fixed_codescheme_$index"}"""
    override fun yclCodePagesData() = listOf(
        """{"marker": "fixed_codepage_0"}""",
        """{"marker": "fixed_codepage_1"}"""
    ).iterator()
}
