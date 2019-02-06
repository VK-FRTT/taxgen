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

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "Compiling DPM database: OK"
        )

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
            "#OwnerNameInDB",
            "EuroFiling",
            "DM Integration Fixture"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
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

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "Compiling DPM database: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).isBlank()

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single command with proper argument must be given"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
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

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "FATAL: Target file '$targetDbPath' already exists"
        )

        assertThat(errText).isBlank()

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when given target database path points to folder`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "${tempFolder.path()}",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "FATAL: Target file '${tempFolder.path()}' already exists"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database"
        )

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).isBlank()

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Option source-folder requires an argument"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when given source filepath does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).isBlank()

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Option source-folder: Directory", "does not exist"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
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

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database"
        )

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
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

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).isBlank()
        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should produce database from DPM source config`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "Processing RDS sources: RDS source data to DPM model",
            "DPM Sources: Reference Data service",
            "Configuration file: (dm_integration_fixture.json)",
            "Configuration file: OK",
            "Compiling DPM database: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
            "#OwnerNameInDB",
            "EuroFiling",
            "DM Integration Fixture"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should report error when source config file is broken JSON`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_source_config_json.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "DPM Sources: Reference Data service",
            "Configuration file: (broken_source_config_json.json)",
            "FATAL: Processing JSON content failed: "
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config file does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "${tempFolder.resolve("non_existing_config.json")}"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Option source-config: File", "does not exist"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should report error when source config links to non existing DPM code list`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unknown_codelist.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM dictionary", "codelist_uri_unknown_codelist",
            "Codelist",
            "Content URLs",
            "FATAL: JSON content fetch failed: HTTP 404 (Not Found)"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should report error when source config links to unresolvable DPM host name`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unresolvable_host.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM dictionary", "codelist_uri_unresolvable_host",
            "Codelist",
            "Content URLs",
            "FATAL: Could not determine the server IP address"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should report error when source config has URI with bad protocol`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_bad_protocol.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM dictionary", "codelist_uri_bad_protocol",
            "Codelist",
            "Content URLs",
            "FATAL: Malformed URI"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should report error when source config URI points to non-responsive host IP`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_non_responsive_host_ip.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM dictionary", "codelist_uri_non_responsive_host_ip",
            "Codelist",
            "Content URLs",
            "FATAL: Could not connect the server"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
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
