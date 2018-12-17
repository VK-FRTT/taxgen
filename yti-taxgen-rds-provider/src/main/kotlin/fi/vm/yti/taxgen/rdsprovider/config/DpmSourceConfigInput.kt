package fi.vm.yti.taxgen.rdsprovider.config

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic

@Suppress("MemberVisibilityCanBePrivate")
data class DpmSourceConfigInput(
    val dpmDictionaries: List<DpmDictionarySourceConfigInput?>?
) {
    fun toValidConfig(diagnostic: Diagnostic): DpmSourceConfig {

        validateValueNotNull(this::dpmDictionaries, diagnostic)
        validateListElementsNotNull(this::dpmDictionaries, diagnostic)

        return DpmSourceConfig(
            dpmDictionaries = dpmDictionaries!!.map {
                it!!.toConfig(diagnostic)
            }
        )
    }
}
