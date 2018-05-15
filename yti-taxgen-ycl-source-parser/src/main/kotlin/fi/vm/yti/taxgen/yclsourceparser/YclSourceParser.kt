package fi.vm.yti.taxgen.yclsourceparser

import fi.vm.yti.taxgen.yclsourceparser.mapping.explicitDomainFromCodeList
import fi.vm.yti.taxgen.yclsourceparser.mapping.ownerFromTaxonomyUnit
import fi.vm.yti.taxgen.yclsourceparser.sourcebundle.SourceBundle

class YclSourceParser {

    fun parse(sourceBundle: SourceBundle) {
        val taxonomyUnits = sourceBundle.taxonomyUnits()

        taxonomyUnits.forEach { unit ->
            val owner = ownerFromTaxonomyUnit(unit)

            val explicitDomains = unit.codeLists().map { codeList -> explicitDomainFromCodeList(codeList) }
        }
    }
}
