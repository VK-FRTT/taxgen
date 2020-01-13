package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Command ´--help´")
internal class TaxgenCli_Help_Test : TaxgenCli_TestBase(
    primaryCommand = "--help"
) {

    @Test
    fun `Should list available command line options`() {
        val args = arrayOf("--help")

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "--help",
                "--version",
                "--create-dictionary-to-new-dpm-db",
                "--capture-dpm-sources-to-folder",
                "--capture-dpm-sources-to-zip",
                "--source-config",
                "--source-folder",
                "--source-zip",
                "--output <Path>",
                "--force-overwrite",
                "--verbosity [[NORMAL,DEBUG]]"
            )
        }
    }
}
