package fi.vm.yti.taxgen.rdsprovider.configinput

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsprovider.CodeListConfig

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
