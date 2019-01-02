package fi.vm.yti.taxgen.rdsdpmmapper.sourcereader

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmSource

internal class DpmSourceReader(
    private val dpmSource: DpmSource,
    private val diagnostic: Diagnostic
) {
    fun eachDpmDictionarySource(action: (DpmDictionarySourceReader) -> Unit) {
        dpmSource.eachDpmDictionarySource {
            action(DpmDictionarySourceReader(it, diagnostic))
        }
    }
}
