package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--capture-ycl-sources-to-folder´")
internal class TaxgenCli_CaptureYclSourcesToFolder_UnitTest : TaxgenCli_UnitTestBase(
    primaryCommand = "--capture-ycl-sources-to-folder"
) {

    private lateinit var targetFolderPath: Path
    private lateinit var targetFolderInfoFilePath: Path

    @BeforeEach
    fun init() {
        targetFolderPath = workFolderPath.resolve("ycl_sources")
        targetFolderInfoFilePath = targetFolderPath.resolve("source_info.json")
    }

    @Test
    fun `Should capture YCL sources to folder`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderInfoFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should overwrite existing files in target folder when force option is given`() {
        Files.createDirectories(targetFolderInfoFilePath.parent)
        Files.write(targetFolderInfoFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
            "--force-overwrite",
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderInfoFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target folder path is not given`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
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
    fun `Should fail when target folder contains conflicting file`() {
        Files.createDirectories(targetFolderInfoFilePath.parent)
        Files.write(targetFolderInfoFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderInfoFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when given target folder path points to file`() {
        val workFolderFilePath = workFolderPath.resolve("file.txt")
        Files.write(workFolderFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            workFolderFilePath.toString(),
            "--source-folder",
            TestFixtures.yclSourceCapturePath("single_comprehensive_tree").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString()
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
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
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
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
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
            "--capture-ycl-sources-to-folder",
            targetFolderPath.toString(),
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
