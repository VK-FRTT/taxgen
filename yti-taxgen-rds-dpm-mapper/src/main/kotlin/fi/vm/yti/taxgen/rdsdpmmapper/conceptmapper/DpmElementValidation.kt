package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.DpmElement

fun validateDpmElements(
    diagnostic: Diagnostic,
    elements: List<DpmElement>
) {
    elements.forEach {
        val context = it.validationContextInfo()
        diagnostic.validate(it, context)
    }
}
