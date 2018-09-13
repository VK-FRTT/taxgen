package fi.vm.yti.taxgen.yclsourceprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.config.DpmDictionarySourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class DpmDictionarySourceConfigInput(
    val owner: OwnerConfigInput?,
    val codelists: List<YclCodelistSourceConfigInput?>?
) {
    fun toValidConfig(diagnostic: Diagnostic): DpmDictionarySourceConfig {
        validateValueNotNull(this::owner, diagnostic)
        validateValueNotNull(this::codelists, diagnostic)

        validateListElementsNotNull(this::codelists, diagnostic)

        return DpmDictionarySourceConfig(
            owner = owner!!.toValidConfig(diagnostic),
            codelists = codelists!!.map {
                it!!.toValidConfig(diagnostic)
            }
        )
    }
}
