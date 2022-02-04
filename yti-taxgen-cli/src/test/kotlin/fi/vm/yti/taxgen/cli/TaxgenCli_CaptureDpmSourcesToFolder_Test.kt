package fi.vm.yti.taxgen.cli

import java.nio.file.Files
import java.nio.file.Path
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

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
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--output",
            "$targetFolderPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: folder",
                "DPM source recorder: folder",
                "Capturing DPM sources: OK"
            )

            assertThat(targetFolderPath).exists().isDirectory()
            assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()
        }
    }

    @Tag("e2etest")
    @Test
    fun `Should capture DPM sources to folder from DPM source config`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-config",
            "$integrationFixtureConfigPath",
            "--output",
            "$targetFolderPath",
            "--verbosity",
            "DEBUG"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: Reference Data service",
                "DPM source recorder: folder",
                "Capturing DPM sources: OK"
            )

            assertThat(targetFolderPath).exists().isDirectory()
            assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should overwrite existing files in output folder when force option is given`() {
        Files.createDirectories(targetFolderMetaConfigFilePath.parent)
        Files.write(targetFolderMetaConfigFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--output",
            "$targetFolderPath",
            "--force-overwrite"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: folder",
                "DPM source recorder: folder",
                "Capturing DPM sources: OK"
            )

            assertThat(targetFolderPath).exists().isDirectory()
            assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should fail when output option is not given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option output must be given"
            )
        }
    }

    @Test
    fun `Should fail when output option without filepath is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--output"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option output requires an argument"
            )
        }
    }

    @Test
    fun `Should report error when output folder contains conflicting file`() {
        Files.createDirectories(targetFolderMetaConfigFilePath.parent)
        Files.write(targetFolderMetaConfigFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--output",
            "$targetFolderPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: folder",
                "DPM source recorder: folder",
                "FATAL: Target file 'dpm_sources/meta/source_config.json' already exists"
            )

            assertThat(targetFolderPath).exists().isDirectory()
            assertThat(targetFolderMetaConfigFilePath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given output folder path points to existing file`() {
        val workFolderFilePath = tempFolder.resolve("file.txt")
        Files.write(workFolderFilePath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--output",
            workFolderFilePath.toString()
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "FATAL: Could not create filesystem path",
                "(already exists)"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--output",
            "$targetFolderPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "One source option with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-folder",
            "--output",
            "$targetFolderPath",
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
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}",
            "--output",
            "$targetFolderPath"
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
            "--capture-dpm-sources-to-folder",
            "--source-folder",
            "$integrationFixtureCapturePath",
            "--source-config",
            "$integrationFixtureConfigPath",
            "--output",
            "$targetFolderPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "One source option with proper argument must be given"
            )
        }
    }
}
