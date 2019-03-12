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
    fun `Should fail when multiple commands with valid arguments are given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "${tempFolder.resolve("output_dpm.db")}",
            "--capture-dpm-sources-to-folder",
            "${tempFolder.resolve("output_capture")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single command with proper argument must be given"
            )
            assertThat(outText).isBlank()
        }
    }
}
