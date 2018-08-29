package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixture.Type.YCL_SOURCE_CONFIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--compile-dpm-db´")
internal class TaxgenCli_ProduceDpmDb_UnitTest : TaxgenCli_UnitTestBase(
    primaryCommand = "--compile-dpm-db"
) {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = tempFolder.resolve("dpm.db")
    }

    @Test
    fun `Should produce database from YCL source capture`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "DPM database written"
        )

        assertThat(targetDbPath).exists().isRegularFile()
        //TODO - Verify targetDb content too

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should overwrite target database file when force option is given`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--force-overwrite",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Compiling DPM database")

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single command with proper argument must be given"
        )
        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when target database file already exists`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).isBlank()

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when given target database path points to folder`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "${tempFolder.path()}",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-folder"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Option source-folder requires an argument"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when given source filepath does not exist`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Option source-folder: Directory", "does not exist"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when more than one source option is given`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$yclSourceCapturePath",
            "--source-config",
            "$yclSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Disabled
    @Test
    fun `Should fail when source capture folder is empty`() {
        val args = arrayOf(
            "--compile-dpm-db",
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
    fun `Should produce database from YCL source config`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$yclSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "DPM database written"
        )

        assertThat(targetDbPath).exists().isRegularFile()
        //TODO - Verify targetDb content too

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config file is broken JSON`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(YCL_SOURCE_CONFIG, "ycl_source_config_broken_json.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Compiling DPM database",
            "Mapping YTI Codelists",
            "YCL Source",
            "Configuration file (ycl_source_config_broken_json.json)",
            "FATAL: Processing JSON content failed: "
        )

        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config file does not exist`() {
        val args = arrayOf(
            "--compile-dpm-db",
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
    fun `Should fail when source config links to non existing YCL code list`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(YCL_SOURCE_CONFIG, "codelist_uri_unknown_codelist.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM Dictionary", "codelist_uri_unknown_codelist",
            "Codelist",
            "URI Resolution",
            "FATAL: JSON content fetch failed: HTTP 404 (Not Found)"
        )

        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config links to unresolvable YCL host name`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(YCL_SOURCE_CONFIG, "codelist_uri_unresolvable_host.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM Dictionary", "codelist_uri_unresolvable_host",
            "Codelist",
            "URI Resolution",
            "FATAL: Could not determine the server IP address"
        )

        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config has URI with broken protocol`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(YCL_SOURCE_CONFIG, "codelist_uri_bad_protocol.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM Dictionary", "codelist_uri_bad_protocol",
            "Codelist",
            "URI Resolution",
            "FATAL: Malformed URI"
        )

        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when source config links to non responsive YCL host IP`() {
        val args = arrayOf(
            "--compile-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(YCL_SOURCE_CONFIG, "codelist_uri_non_responsive_host_ip.json")
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "DPM Dictionary", "codelist_uri_non_responsive_host_ip",
            "Codelist",
            "URI Resolution",
            "FATAL: Could not connect the server"
        )

        assertThat(errText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }
}
