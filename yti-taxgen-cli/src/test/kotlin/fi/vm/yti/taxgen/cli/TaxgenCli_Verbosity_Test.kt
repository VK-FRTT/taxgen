package fi.vm.yti.taxgen.cli

import java.nio.file.Path
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@DisplayName("Command ´--verbosity´")
internal class TaxgenCli_Verbosity_Test : TaxgenCli_TestBase(
    primaryCommand = null
) {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = tempFolder.resolve("dpm.db")
    }

    @Tag("e2etest")
    @Test
    fun `Should produce more detailed diagnostics with verbosity DEBUG option`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--source-config",
            "$integrationFixtureConfigPath",
            "--output",
            "$targetDbPath",
            "--verbosity",
            "DEBUG"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Configuration file: (integration_fixture.json)",
                "Configuration file: OK",
                "DPM source: Reference Data service",
                "RDS to DPM mapper",
                "DEBUG: Fetching JSON: https://koodistot.test.yti.cloud.vrk.fi/codelist-api/api/v1/coderegistries/dpm-integration-fixture/codeschemes/exp-doms-2018-1?expand=extension",
                "DEBUG: Fetching JSON: https://koodistot.test.yti.cloud.vrk.fi/codelist-api/api/v1/coderegistries/dpm-integration-fixture/codeschemes/exp-doms-2018-1?expand=code&pretty",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Tag("e2etest")
    @Test
    fun `Should not produce debug diagnostics when verbosity option is not given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--source-config",
            "$integrationFixtureConfigPath",
            "--output",
            "$targetDbPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Configuration file: (integration_fixture.json)",
                "Configuration file: OK",
                "DPM source: Reference Data service",
                "RDS to DPM mapper",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(outText).doesNotContain(
                "Content URLs: DEBUG: Fetching JSON:",
                "Codelist #0: DEBUG: Fetching JSON:"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }
}
