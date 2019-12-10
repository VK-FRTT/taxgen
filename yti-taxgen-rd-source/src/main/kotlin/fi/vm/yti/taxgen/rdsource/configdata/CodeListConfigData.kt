package fi.vm.yti.taxgen.rdsource.configdata

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsource.CodeListConfig

data class CodeListConfigData(
    val uri: String?
) {

    @Suppress("UNUSED_PARAMETER")
    fun toConfig(diagnostic: Diagnostic): CodeListConfig {
        return CodeListConfig(
            uri = uri
        )
    }
}
