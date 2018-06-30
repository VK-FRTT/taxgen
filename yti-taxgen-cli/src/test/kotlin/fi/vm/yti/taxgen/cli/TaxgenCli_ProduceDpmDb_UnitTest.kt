package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--produce-dpm-db´")
internal class TaxgenCli_ProduceDpmDb_UnitTest : TaxgenCli_UnitTestBase() {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = workFolderPath.resolve("dpm.db")
    }

    @Test
    fun `Should produce database from YCL source capture`() {
        val args = arrayOf(
            "--produce-dpm-db",
            targetDbPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Producing DPM database")

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should overwrite target datbase file when force option is given`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--produce-dpm-db",
            targetDbPath.toString(),
            "--force-overwrite",
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Producing DPM database")

        assertThat(targetDbPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--produce-dpm-db",
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
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
            "--produce-dpm-db",
            targetDbPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
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
            "--produce-dpm-db",
            workFolderPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--produce-dpm-db",
            targetDbPath.toString()
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
            "--produce-dpm-db",
            targetDbPath.toString(),
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
            "--produce-dpm-db",
            targetDbPath.toString(),
            "--source-folder",
            workFolderPath.resolve("non_existing_folder").toString()
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
            "--produce-dpm-db",
            targetDbPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString(),
            "--source-config",
            TestFixtures.yclSourceConfigPath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }
}
