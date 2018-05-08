package fi.vm.yti.taxgen.yclsourceparser.sourcebundle

import fi.vm.yti.taxgen.datapointmetamodel.Owner

interface TaxonomyUnit {
    fun owner(): Owner
    fun codeLists(): Iterator<CodeList>
}
