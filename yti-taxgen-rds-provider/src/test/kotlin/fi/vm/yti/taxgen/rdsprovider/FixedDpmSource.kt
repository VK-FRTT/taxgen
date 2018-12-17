package fi.vm.yti.taxgen.rdsprovider

class FixedDpmSource : DpmSource() {

    override fun contextLabel() = "FixedDpmSource"
    override fun contextIdentifier() = ""

    override fun sourceConfigData() = """{"marker": "fixed_source_config"}"""
    override fun dpmDictionarySources() = listOf(FixedDpmDictionarySource(0), FixedDpmDictionarySource(1))
    override fun close() {}
}

class FixedDpmDictionarySource(private val dIndex: Int) : DpmDictionarySource {
    override fun dpmOwnerConfigData() = """{"marker": "fixed_dpm_owner_config_d$dIndex"}"""
    override fun explicitDomainAndHierarchiesSources() = listOf(FixedCodeListSource(dIndex, 0), FixedCodeListSource(dIndex, 1))
}

class FixedCodeListSource(private val dIndex: Int, private val cIndex: Int) : CodeListSource {
    override fun codeListData() = """{"marker": "fixed_codescheme_d${dIndex}_c$cIndex"}"""
    override fun codePagesData() = listOf(
        """{"marker": "fixed_codes_page_d${dIndex}_c${cIndex}_p0"}""",
        """{"marker": "fixed_codes_page_d${dIndex}_c${cIndex}_p1"}"""
    ).iterator().asSequence()

    override fun extensionSources() =
        listOf(FixedCodeListExtensionSource(dIndex, cIndex, 0), FixedCodeListExtensionSource(dIndex, cIndex, 1))
}

class FixedCodeListExtensionSource(private val dIndex: Int, private val cIndex: Int, private val eIndex: Int) :
    CodeListExtensionSource {
    override fun extensionData() = """{"marker": "fixed_extension_d${dIndex}_c${cIndex}_e$eIndex"}"""
    override fun extensionMemberPagesData() = listOf(
        """{"marker": "fixed_extension_member_d${dIndex}_c${cIndex}_e${eIndex}_p0"}""",
        """{"marker": "fixed_extension_member_d${dIndex}_c${cIndex}_e${eIndex}_p1"}"""
    ).iterator().asSequence()
}
