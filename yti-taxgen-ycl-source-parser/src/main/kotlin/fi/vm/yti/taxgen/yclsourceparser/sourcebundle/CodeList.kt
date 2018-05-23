package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

interface CodeList {

    fun codeListData(): String
    fun codePagesData(): Iterator<String>
}
