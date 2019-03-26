package fi.vm.yti.taxgen.rdsprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.config.CodeListConfig

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
