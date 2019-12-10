package fi.vm.yti.taxgen.rddpmmapper.modelmapper

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.DpmSource

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
