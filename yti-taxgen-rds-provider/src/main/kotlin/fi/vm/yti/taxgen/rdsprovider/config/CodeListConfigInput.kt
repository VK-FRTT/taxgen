package fi.vm.yti.taxgen.rdsprovider.config

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic

data class CodeListConfigInput(
    val uri: String?
) {

    @Suppress("UNUSED_PARAMETER")
    fun toConfig(diagnostic: Diagnostic): CodeListConfig {
        return CodeListConfig(
            uri = uri
        )
    }
}
