package fi.vm.yti.taxgen.yclsourceprovider

class FixedYclSource : YclSource() {

    override fun contextName() = "FixedYclSource"
    override fun contextRef() = ""

    override fun sourceConfigData() = """{"marker": "fixed_source_config"}"""
    override fun dpmDictionarySources() = listOf(FixedDpmDictionarySource(0), FixedDpmDictionarySource(1))
    override fun close() {}
}

class FixedDpmDictionarySource(private val dIndex: Int) : DpmDictionarySource(dIndex) {
    override fun dpmOwnerConfigData() = """{"marker": "fixed_dpm_owner_config_d$dIndex"}"""
    override fun yclCodelistSources() = listOf(FixedYclCodelistSource(dIndex, 0), FixedYclCodelistSource(dIndex, 1))
}

class FixedYclCodelistSource(private val dIndex: Int, private val cIndex: Int) : YclCodelistSource(cIndex) {
    override fun yclCodelistSourceConfigData(): String = """{"marker": "fixed_codelist_source_config_d${dIndex}_c$cIndex"}"""
    override fun yclCodeSchemeData() = """{"marker": "fixed_codescheme_d${dIndex}_c$cIndex"}"""
    override fun yclCodePagesData() = listOf(
        """{"marker": "fixed_codes_page_d${dIndex}_c${cIndex}_p0"}""",
        """{"marker": "fixed_codes_page_d${dIndex}_c${cIndex}_p1"}"""
    ).iterator().asSequence()

    override fun yclCodelistExtensionSources() =
        listOf(FixedYclCodelistExtensionSource(dIndex, cIndex, 0), FixedYclCodelistExtensionSource(dIndex, cIndex, 1))
}

class FixedYclCodelistExtensionSource(private val dIndex: Int, private val cIndex: Int, private val eIndex: Int) : YclCodelistExtensionSource(eIndex) {
    override fun yclExtensionData() = """{"marker": "fixed_extension_d${dIndex}_c${cIndex}_e$eIndex"}"""
    override fun yclExtensionMemberPagesData() = listOf(
        """{"marker": "fixed_extension_member_d${dIndex}_c${cIndex}_e${eIndex}_p0"}""",
        """{"marker": "fixed_extension_member_d${dIndex}_c${cIndex}_e${eIndex}_p1"}"""
    ).iterator().asSequence()
}
