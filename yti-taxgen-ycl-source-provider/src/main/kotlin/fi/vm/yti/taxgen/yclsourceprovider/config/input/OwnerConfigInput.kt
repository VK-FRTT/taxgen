package fi.vm.yti.taxgen.yclsourceprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.yclsourceprovider.config.OwnerConfig

@Suppress("MemberVisibilityCanBePrivate")
data class OwnerConfigInput(
    val name: String?,
    val namespace: String?,
    val prefix: String?,
    val location: String?,
    val copyright: String?,
    val languages: List<String?>?,
    val defaultLanguage: String?
) {

    fun toValidConfig(diagnostic: Diagnostic): OwnerConfig {
        validateValueNotNull(this::name, diagnostic)
        validateValueNotNull(this::namespace, diagnostic)
        validateValueNotNull(this::prefix, diagnostic)
        validateValueNotNull(this::location, diagnostic)
        validateValueNotNull(this::copyright, diagnostic)
        validateValueNotNull(this::languages, diagnostic)
        validateValueNotNull(this::defaultLanguage, diagnostic)

        validateListElementsNotNull(this::languages, diagnostic)

        @Suppress("UNCHECKED_CAST")
        return OwnerConfig(
            name = name!!,
            namespace = namespace!!,
            prefix = prefix!!,
            location = location!!,
            copyright = copyright!!,
            languages = languages!! as List<String>,
            defaultLanguage = defaultLanguage!!
        )
    }
}
