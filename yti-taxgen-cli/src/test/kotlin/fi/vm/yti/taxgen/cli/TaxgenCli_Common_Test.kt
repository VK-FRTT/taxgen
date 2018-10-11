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
        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "No options given (-h will show valid options)"
        )
        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when multiple commands with valid arguments are given`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "${tempFolder.resolve("output_dpm.db")}",
            "--capture-ycl-sources-to-folder",
            "${tempFolder.resolve("output_ycl_capture")}"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single command with proper argument must be given"
        )
        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }
}
