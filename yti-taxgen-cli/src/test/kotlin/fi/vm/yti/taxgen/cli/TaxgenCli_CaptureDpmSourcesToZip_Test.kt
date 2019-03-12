package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Command ´--capture-dpm-sources-to-zip´")
internal class TaxgenCli_CaptureDpmSourcesToZip_Test : TaxgenCli_TestBase(
    primaryCommand = "--capture-dpm-sources-to-zip"
) {

    private lateinit var targetZipPath: Path

    @BeforeEach
    fun init() {
        targetZipPath = tempFolder.resolve("target.zip")
    }

    @Test
    fun `Should capture DPM sources to zip file from existing capture`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "DPM source: folder",
                "Capturing DPM sources: OK"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should capture DPM sources to zip file from DPM source config`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "DPM source: Reference Data service",
                "Capturing DPM sources: OK"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should overwrite target zip file when force option is given`() {
        Files.write(targetZipPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
            "--force-overwrite",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "DPM source: folder",
                "Capturing DPM sources: OK"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should fail when target zip filename is not given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
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
    fun `Should report error when target zip file already exists`() {
        Files.write(targetZipPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "FATAL: Target file '$targetZipPath' already exists"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given target zip file path points to folder`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "${tempFolder.path()}",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "FATAL: Target file '${tempFolder.path()}' already exists"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "$targetZipPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources"
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
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
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
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
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
            "--capture-dpm-sources-to-zip",
            "$targetZipPath",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }
}
