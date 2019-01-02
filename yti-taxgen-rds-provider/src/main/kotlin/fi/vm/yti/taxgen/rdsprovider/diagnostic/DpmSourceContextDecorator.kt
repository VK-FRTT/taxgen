package fi.vm.yti.taxgen.rdsprovider.diagnostic

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.DpmDictionarySource
import fi.vm.yti.taxgen.rdsprovider.DpmSource

internal class DpmSourceContextDecorator(
    private val dpmSource: DpmSource,
    private val diagnostic: Diagnostic
) : DpmSource() {

    override fun contextLabel() = dpmSource.contextLabel()
    override fun contextIdentifier() = dpmSource.contextIdentifier()

    override fun sourceConfigData(): String {
        return dpmSource.sourceConfigData()
    }

    override fun eachDpmDictionarySource(action: (DpmDictionarySource) -> Unit) {
        diagnostic.withContext(dpmSource) {
            dpmSource.eachDpmDictionarySource {
                action(it)
                //val decorator = DpmDictionarySourceContextDecorator(it, diagnostic)
                //action(decorator)
            }
        }
    }

    override fun close() {
        dpmSource.close()
    }
}
