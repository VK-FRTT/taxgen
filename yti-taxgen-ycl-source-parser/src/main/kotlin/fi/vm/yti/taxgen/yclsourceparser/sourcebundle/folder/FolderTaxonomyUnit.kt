package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit

class FolderTaxonomyUnit() : TaxonomyUnit {

    override fun namespace() = ""
    override fun namespacePrefix() = ""
    override fun officialLocation() = ""
    override fun copyrightText() = ""
    override fun supportedLanguages() = emptyList<String>()

    override fun codeLists(): Iterator<CodeList> {
        return FolderCodeListsIterator()
    }
}
