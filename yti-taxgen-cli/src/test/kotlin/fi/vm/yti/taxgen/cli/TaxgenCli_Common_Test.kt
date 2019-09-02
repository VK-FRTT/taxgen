package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Common command handling")
internal class TaxgenCli_Common_Test : TaxgenCli_TestBase(
    primaryCommand = null
) {

    @Test
    fun `Should use common exit code values`() {
        assertThat(TAXGEN_CLI_SUCCESS).isEqualTo(0)
        assertThat(TAXGEN_CLI_FAIL).isEqualTo(1)
    }

    @Test
    fun `Should fail when no options are given`() {
        val args = emptyArray<String>()

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "No options given (-h will show valid options)"
            )
            assertThat(outText).isBlank()
        }
    }

    @Test
    fun `Should fail when multiple commands are given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--capture-dpm-sources-to-folder"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single command must be given"
            )
            assertThat(outText).isBlank()
        }
    }
}
