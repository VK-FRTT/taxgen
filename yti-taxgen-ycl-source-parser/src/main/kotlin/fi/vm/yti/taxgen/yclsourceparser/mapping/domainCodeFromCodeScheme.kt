package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.yclsourceparser.model.CodeScheme

fun domainCodeFromCodeScheme(codeScheme: CodeScheme): String {
    return codeScheme.codeValue!!
}
