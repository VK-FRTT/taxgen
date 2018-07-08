package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--capture-ycl-sources-to-zip´")
internal class TaxgenCli_CaptureYclSourcesToZip_UnitTest : TaxgenCli_UnitTestBase(
    primaryCommand = "--capture-ycl-sources-to-zip"
) {

    private lateinit var targetZipPath: Path

    @BeforeEach
    fun init() {
        targetZipPath = tempFolder.resolve("target.zip")
    }

    @Test
    fun `Should capture YCL sources to zip file`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(targetZipPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should overwrite target zip file when force option is given`() {
        Files.write(targetZipPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
            "--force-overwrite",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()
        assertThat(outText).containsSubsequence("Capturing YTI Codelist sources")

        assertThat(targetZipPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target zip filename is not given`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-zip",
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
    fun `Should fail when target zip file already exists`() {
        Files.write(targetZipPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isNotBlank() //TODO - proper error message & its verification
        assertThat(outText).isBlank()

        assertThat(targetZipPath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when given target zip file path points to folder`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-zip",
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
            "--capture-ycl-sources-to-zip",
            "$targetZipPath"
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
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
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
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
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
            "--capture-ycl-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$yclSourceCapturePath",
            "--source-config",
            "$yclSsourceConfigPath"
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
