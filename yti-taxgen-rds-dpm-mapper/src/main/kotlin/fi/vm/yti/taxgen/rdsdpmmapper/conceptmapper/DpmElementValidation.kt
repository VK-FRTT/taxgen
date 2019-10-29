package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.DpmElement

fun validateDpmElements(
    diagnostic: Diagnostic,
    elements: List<DpmElement>
) {
    elements.forEach {
        diagnostic.validate(it) {
            it.validationContextInfo()
        }
    }
}
