package fi.vm.yti.taxgen.yclsourceprovider

interface DpmDictionarySource {
    fun dpmOwnerInfoData(): String
    fun yclCodelistSources(): List<YclCodelistSource>
}
