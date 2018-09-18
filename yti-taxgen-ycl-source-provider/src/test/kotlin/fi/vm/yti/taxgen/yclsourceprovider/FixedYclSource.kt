package fi.vm.yti.taxgen.yclsourceprovider

class FixedYclSource : YclSource() {

    override fun contextName() = "FixedYclSource"
    override fun contextRef() = ""

    override fun sourceConfigData() = """{"marker": "fixed_source_config"}"""
    override fun dpmDictionarySources() = listOf(FixedDpmDictionarySource(0), FixedDpmDictionarySource(1))
    override fun close() {}
}

class FixedDpmDictionarySource(private val index: Int) : DpmDictionarySource(index) {
    override fun dpmOwnerConfigData() = """{"marker": "fixed_dpm_owner_config_$index"}"""
    override fun yclCodelistSources() = listOf(FixedYclCodelistSource(0), FixedYclCodelistSource(1))
}

class FixedYclCodelistSource(private val index: Int) : YclCodelistSource(index) {
    override fun yclCodelistSourceConfigData(): String = """{"marker": "fixed_codelist_source_config_$index"}"""
    override fun yclCodeSchemeData() = """{"marker": "fixed_codescheme_$index"}"""
    override fun yclCodePagesData() = listOf(
        """{"marker": "fixed_codepage_0"}""",
        """{"marker": "fixed_codepage_1"}"""
    ).iterator().asSequence()

    override fun yclCodelistExtensionSources() =
        listOf(FixedYclCodelistExtensionSource(0), FixedYclCodelistExtensionSource(1))
}

class FixedYclCodelistExtensionSource(private val index: Int) : YclCodelistExtensionSource(index) {
    override fun yclExtensionData() = """{"marker": "fixed_extension_$index"}"""
    override fun yclExtensionMemberPagesData() = listOf(
        """{"marker": "fixed_extension_member_${index}_0"}""",
        """{"marker": "fixed_extension_member_${index}_1"}"""
    ).iterator().asSequence()
}
