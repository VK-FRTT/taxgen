package fi.vm.yti.taxgen.rdsdpmmapper.modelmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource

internal class DpmSourceModelMapper(
    private val dpmSource: DpmSource,
    private val diagnostic: Diagnostic
) {
    fun eachDpmDictionaryModelMapper(action: (DpmDictionaryModelMapper) -> Unit) {
        dpmSource.eachDpmDictionarySource {
            action(
                DpmDictionaryModelMapper(
                    dpmDictionarySource = it,
                    diagnostic = diagnostic
                )
            )
        }
    }
}
