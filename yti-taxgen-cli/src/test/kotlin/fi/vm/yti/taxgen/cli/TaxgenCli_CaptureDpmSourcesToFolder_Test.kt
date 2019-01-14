package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--capture-dpm-sources-to-folder´")
internal class TaxgenCli_CaptureDpmSourcesToFolder_Test : TaxgenCli_TestBase(
    primaryCommand = "--capture-dpm-sources-to-folder"
) {
    private lateinit var targetFolderPath: Path
    private lateinit var targetFolderMetaConfigFilePath: Path

    @BeforeEach
    fun init() {
        targetFolderPath = tempFolder.resolve("dpm_sources")
        targetFolderMetaConfigFilePath = targetFolderPath.resolve("meta/source_config.json")
    }

    @Test
    fun `Should capture DPM sources to folder from existing capture`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources",
            "Writing DPM sources: folder",
            "DPM Sources: folder",
            "Capturing DPM sources: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should capture DPM sources to folder from DPM source config`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources",
            "Writing DPM sources: folder",
            "DPM Sources: Reference Data service",
            "Capturing DPM sources: OK"
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
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath",
            "--force-overwrite",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources",
            "Writing DPM sources: folder",
            "DPM Sources: folder",
            "Capturing DPM sources: OK"
        )

        assertThat(errText).isBlank()

        assertThat(targetFolderPath).exists().isDirectory()
        assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when target folder path is not given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
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
    fun `Should report error when target folder contains conflicting file`() {
        Files.createDirectories(targetFolderMetaConfigFilePath.parent)
        Files.write(targetFolderMetaConfigFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).isBlank()

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources",
            "Writing DPM sources: folder",
            "DPM Sources: folder",
            "FATAL: Target file 'dpm_sources/meta/source_config.json' already exists"
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
            "--capture-dpm-sources-to-folder",
            workFolderFilePath.toString(),
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources",
            "FATAL: Could not create filesystem path",
            "(already exists)"
        )

        assertThat(errText).isBlank()

        assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources"
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
            "--capture-dpm-sources-to-folder",
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
            "--capture-dpm-sources-to-folder",
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
            "--capture-dpm-sources-to-folder",
            "$targetFolderPath",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(outText).containsSubsequence(
            "Capturing DPM sources"
        )

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single source with proper argument must be given"
        )

        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }
}
