package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

interface CodeList {

    fun codeList(): String
    fun codesPages(): Iterator<String>
}
