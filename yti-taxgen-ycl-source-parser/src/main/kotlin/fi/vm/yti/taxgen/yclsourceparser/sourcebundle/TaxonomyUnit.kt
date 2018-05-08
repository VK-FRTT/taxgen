package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

interface TaxonomyUnit {
    fun namespace(): String
    fun namespacePrefix(): String
    fun officialLocation(): String
    fun copyrightText(): String
    fun supportedLanguages(): List<String>
    fun codeLists(): Iterator<CodeList>
}
