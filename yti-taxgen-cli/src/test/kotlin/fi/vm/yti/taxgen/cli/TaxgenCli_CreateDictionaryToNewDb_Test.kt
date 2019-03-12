package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_SOURCE_CONFIG
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager

@DisplayName("Command ´--create-dictionary-to-new-dpm-db´")
internal class TaxgenCli_CreateDictionaryToNewDb_Test : TaxgenCli_TestBase(
    primaryCommand = "--create-dictionary-to-new-dpm-db"
) {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = tempFolder.resolve("dpm.db")
    }

    @Test
    fun `Should produce database from DPM source capture`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )
        }
    }

    @Test
    fun `Should overwrite target database file when force option is given`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--force-overwrite",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single command with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should report error when target database file already exists`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: Target file '$targetDbPath' already exists"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given target database path points to folder`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "${tempFolder.path()}",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: Target file '${tempFolder.path()}' already exists"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder requires an argument"
            )
        }
    }

    @Test
    fun `Should fail when given source filepath does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder: Directory", "does not exist"
            )
        }
    }

    @Test
    fun `Should fail when more than one source option is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Disabled
    @Test
    fun `Should fail when source capture folder is empty`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.path()}"
        )

        executeCliAndExpectFail(args) { outText, errText ->
            assertThat(outText).isBlank()
            assertThat(errText).isBlank()
        }
    }

    @Test
    fun `Should produce database from DPM source config`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "RDS to DPM mapper",
                "DPM source: Reference Data service",
                "Configuration file: (dm_integration_fixture.json)",
                "Configuration file: OK",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )
        }
    }

    @Test
    fun `Should report error when source config file is broken JSON`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_source_config_json.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "DPM source: Reference Data service",
                "Configuration file: (broken_source_config_json.json)",
                "FATAL: Processing JSON content failed: "
            )
        }
    }

    @Test
    fun `Should fail when source config file does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "${tempFolder.resolve("non_existing_config.json")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-config: File", "does not exist"
            )

            assertThat(outText).isBlank()
        }
    }

    @Test
    fun `Should report error when source config links to non existing DPM code list`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unknown_codelist.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unknown_codelist",
                "Codelist",
                "Content URLs",
                "FATAL: JSON content fetch failed: HTTP 404 (Not Found)"
            )
        }
    }

    @Test
    fun `Should report error when source config links to unresolvable DPM host name`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unresolvable_host.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unresolvable_host",
                "Codelist",
                "Content URLs",
                "FATAL: Could not determine the server IP address"
            )
        }
    }

    @Test
    fun `Should report error when source config has URI with bad protocol`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_bad_protocol.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_bad_protocol",
                "Codelist",
                "Content URLs",
                "FATAL: Malformed URI"
            )
        }
    }

    @Test
    fun `Should report error when source config URI points to non-responsive host IP`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_non_responsive_host_ip.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_non_responsive_host_ip",
                "Codelist",
                "Content URLs",
                "FATAL: Could not connect the server"
            )
        }
    }

    private fun fetchDpmOwnersFromTargetDb(): List<String> {
        val dbConnection = DriverManager.getConnection("jdbc:sqlite:$targetDbPath")
        val rows = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    mOwner.OwnerName AS 'OwnerNameInDB'
                FROM mOwner
                ORDER BY mOwner.OwnerName DESC
                """
        ).toStringList()

        return rows
    }
}
