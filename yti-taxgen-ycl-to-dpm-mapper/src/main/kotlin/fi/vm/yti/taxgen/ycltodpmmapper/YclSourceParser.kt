package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.mapping.explicitDomainFromCodeList
import fi.vm.yti.taxgen.ycltodpmmapper.mapping.ownerFromTaxonomyUnit

class YclSourceParser {

    fun parse(yclSource: YclSource) {
        val dpmDictionarySources = yclSource.dpmDictionarySources()

        dpmDictionarySources.forEach { unit ->
            val owner = ownerFromTaxonomyUnit(unit)

            val explicitDomains = unit.yclCodelistSources().map { codeList -> explicitDomainFromCodeList(codeList) }
        }
    }
}
