package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclTaxonomyUnitConfig

class YclServiceTaxonomyUnitIterator(
    taxonomyUnitConfigs: List<YclTaxonomyUnitConfig>
) : AbstractIterator<TaxonomyUnit>() {

    private val taxonomyUnitConfigsIterator = taxonomyUnitConfigs.iterator()

    override fun computeNext() {

        if (taxonomyUnitConfigsIterator.hasNext()) {
            setNext(YclServiceTaxonomyUnit(taxonomyUnitConfigsIterator.next()))
        } else {
            done()
        }
    }
}
