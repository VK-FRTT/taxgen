package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--version´")
internal class TaxgenCli_Version_Test : TaxgenCli_TestBase(
    primaryCommand = "--version"
) {

    @Test
    fun `Should show version info`() {
        val args = arrayOf("--version")

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "YTI TaxGen CLI",
                "Version:      0.0.0-DEV",
                "Build time:   -",
                "Revision:     -"
            )
        }
    }
}
