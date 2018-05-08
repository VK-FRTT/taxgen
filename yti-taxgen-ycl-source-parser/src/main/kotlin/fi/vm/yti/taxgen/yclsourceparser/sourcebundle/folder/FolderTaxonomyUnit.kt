package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.folder

import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit

class FolderTaxonomyUnit() : TaxonomyUnit {

    override fun owner(): Owner = Owner(
        namespace = "",
        namespacePrefix = "",
        officialLocation = "",
        copyrightText = "",
        supportedLanguages = emptyList()
    )

    override fun codeLists(): Iterator<CodeList> {
        return FolderCodeListsIterator()
    }
}
