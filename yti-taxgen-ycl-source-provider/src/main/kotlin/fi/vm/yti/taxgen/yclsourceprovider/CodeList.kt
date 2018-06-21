package fi.vm.yti.taxgen.yclsourceprovider

interface CodeList {

    fun codeListData(): String
    fun codePagesData(): Iterator<String>
}
