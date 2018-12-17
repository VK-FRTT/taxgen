package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--capture-ycl-sources-to-folder´")
internal class TaxgenCli_CaptureDpmSourcesToFolder_Test : TaxgenCli_TestBase(
    primaryCommand = "--capture-ycl-sources-to-folder"
) {
    private lateinit var targetFolderPath: Path
    private lateinit var targetFolderMetaConfigFilePath: Path

    @BeforeEach
    fun init() {
        targetFolderPath = tempFolder.resolve("ycl_sources")
        targetFolderMetaConfigFilePath = targetFolderPath.resolve("meta/source_config.json")
    }

    @Test
    fun `Should capture YCL sources to folder from existing capture`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources",
            "Writing YCL sources: folder",
            "YCL Sources: folder",
            "Capturing YTI Codelist sources: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should capture YCL sources to folder from YCL source config`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
            "--source-config",
            "$yclSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources",
            "Writing YCL sources: folder",
            "YCL Sources: YTI Reference Data service",
            "Capturing YTI Codelist sources: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should overwrite existing files in target folder when force option is given`() {
        Files.createDirectories(targetFolderMetaConfigFilePath.parent)
        Files.write(targetFolderMetaConfigFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
            "--force-overwrite",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources",
            "Writing YCL sources: folder",
            "YCL Sources: folder",
            "Capturing YTI Codelist sources: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target folder path is not given`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "--source-folder",
            "$yclSourceCapturePath"
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
    fun `Should report error when target folder contains conflicting file`() {
        Files.createDirectories(targetFolderMetaConfigFilePath.parent)
        Files.write(targetFolderMetaConfigFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources",
            "Writing YCL sources: folder",
            "YCL Sources: folder",
            "FATAL: Target file 'ycl_sources/meta/source_config.json' already exists"
        )

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should report error when given target folder path points to existing file`() {
        val workFolderFilePath = tempFolder.resolve("file.txt")
        Files.write(workFolderFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            workFolderFilePath.toString(),
            "--source-folder",
            "$yclSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources",
            "FATAL: Could not create filesystem path",
            "(already exists)"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources"
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
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
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
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
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
            "--capture-ycl-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$yclSourceCapturePath",
            "--source-config",
            "$yclSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing YTI Codelist sources"
        )

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }
}
