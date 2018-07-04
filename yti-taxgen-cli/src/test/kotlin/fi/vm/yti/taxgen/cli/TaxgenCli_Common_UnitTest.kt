package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Common command handling")
internal class TaxgenCli_Common_UnitTest : TaxgenCli_UnitTestBase(
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
            "--produce-dpm-db",
            workFolderPath.resolve("output_dpm.db").toString(),
            "--capture-ycl-sources-to-folder",
            workFolderPath.resolve("output_ycl_capture").toString()
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
