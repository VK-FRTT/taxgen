package fi.vm.yti.taxgen.yclsourceprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.config.YclSourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class YclSourceConfigInput(
    val dpmDictionaries: List<DpmDictionarySourceConfigInput?>?
) {
    fun toValidConfig(diagnostic: Diagnostic): YclSourceConfig {

        validateValueNotNull(this::dpmDictionaries, diagnostic)
        validateListElementsNotNull(this::dpmDictionaries, diagnostic)

        return YclSourceConfig(
            dpmDictionaries = dpmDictionaries!!.map {
                it!!.toValidConfig(diagnostic)
            }
        )
    }
}
