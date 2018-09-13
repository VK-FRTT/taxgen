package fi.vm.yti.taxgen.yclsourceprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.config.YclCodelistSourceConfig

@Suppress("MemberVisibilityCanBePrivate")
data class YclCodelistSourceConfigInput(
    val uri: String?,
    val domainCode: String?,
    val memberCodePrefix: String?
) {
    fun toValidConfig(diagnostic: Diagnostic): YclCodelistSourceConfig {
        validateValueNotNull(this::uri, diagnostic)
        validateValueNotNull(this::domainCode, diagnostic)

        return YclCodelistSourceConfig(
            uri = uri!!,
            domainCode = domainCode!!,
            memberCodePrefix = memberCodePrefix
        )
    }
}
