package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclTaxonomyUnitConfig

class YclServiceTaxonomyUnit(
    private val yclTaxonomyUnitConfig: YclTaxonomyUnitConfig
) : TaxonomyUnit {

    override fun owner(): Owner = yclTaxonomyUnitConfig.owner
    override fun codeLists(): Iterator<CodeList> {
        return YclServiceCodeListsIterator(yclTaxonomyUnitConfig.codeLists)
    }
}
