package fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice

import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.CodeList
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.TaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.yclservice.config.YclTaxonomyUnitConfig

class YclServiceTaxonomyUnit(
    private val yclTaxonomyUnitConfig: YclTaxonomyUnitConfig
) : TaxonomyUnit {

    override fun namespace() = yclTaxonomyUnitConfig.namespace
    override fun namespacePrefix() = yclTaxonomyUnitConfig.namespacePrefix
    override fun officialLocation() = yclTaxonomyUnitConfig.officialLocation
    override fun copyrightText() = yclTaxonomyUnitConfig.copyrightText
    override fun supportedLanguages() = yclTaxonomyUnitConfig.supportedLanguages

    override fun codeLists(): Iterator<CodeList> {
        return YclServiceCodeListsIterator(yclTaxonomyUnitConfig.codeLists)
    }
}
