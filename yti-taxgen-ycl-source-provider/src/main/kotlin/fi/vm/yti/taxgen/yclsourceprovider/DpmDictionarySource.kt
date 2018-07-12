package fi.vm.yti.taxgen.yclsourceprovider

interface DpmDictionarySource {
    fun dpmOwnerConfigData(): String
    fun yclCodelistSources(): List<YclCodelistSource>
}
