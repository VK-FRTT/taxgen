package fi.vm.yti.taxgen.yclsourceparser.mapping

import fi.vm.yti.taxgen.yclsourceparser.model.YclCodeScheme

fun domainCodeFromYclCodeScheme(yclCodeScheme: YclCodeScheme): String {
    return yclCodeScheme.codeValue!!
}
