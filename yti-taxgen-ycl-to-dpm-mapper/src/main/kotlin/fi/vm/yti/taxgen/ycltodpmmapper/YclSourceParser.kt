package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.ycltodpmmapper.mapping.explicitDomainFromCodeList
import fi.vm.yti.taxgen.ycltodpmmapper.mapping.ownerFromTaxonomyUnit
import fi.vm.yti.taxgen.yclsourceprovider.SourceBundle

class YclSourceParser {

    fun parse(sourceBundle: SourceBundle) {
        val taxonomyUnits = sourceBundle.taxonomyUnits()

        taxonomyUnits.forEach { unit ->
            val owner = ownerFromTaxonomyUnit(unit)

            val explicitDomains = unit.codeLists().map { codeList -> explicitDomainFromCodeList(codeList) }
        }
    }
}
