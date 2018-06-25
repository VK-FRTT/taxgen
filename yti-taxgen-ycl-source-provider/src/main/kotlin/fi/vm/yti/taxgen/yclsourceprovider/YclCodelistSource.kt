package fi.vm.yti.taxgen.yclsourceprovider

interface YclCodelistSource {

    fun yclCodeschemeData(): String
    fun yclCodePagesData(): Iterator<String>
}
