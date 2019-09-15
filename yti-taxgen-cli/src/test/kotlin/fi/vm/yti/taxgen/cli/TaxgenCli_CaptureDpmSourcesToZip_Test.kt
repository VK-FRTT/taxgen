package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
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
            "--source-folder",
            "$dpmSourceCapturePath",
            "--output",
            "$targetZipPath"
            )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: folder",
                "DPM source recorder: ZIP file",
                "Capturing DPM sources: OK"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Tag("e2etest")
    @Test
    fun `Should capture DPM sources to zip file from DPM source config`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "--source-config",
            "$dpmSourceConfigPath",
            "--output",
            "$targetZipPath"
            )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: Reference Data service",
                "DPM source recorder: ZIP file",
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
            "--source-folder",
            "$dpmSourceCapturePath",
            "--output",
            "$targetZipPath",
            "--force-overwrite"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source: folder",
                "DPM source recorder: ZIP file",
                "Capturing DPM sources: OK"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should fail when output option is not given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "--source-folder",
            "$dpmSourceCapturePath"
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
            "--capture-dpm-sources-to-zip",
            "--source-folder",
            "$dpmSourceCapturePath",
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
    fun `Should report error when target zip file already exists`() {
        Files.write(targetZipPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--output",
            "$targetZipPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "FATAL: Output file '$targetZipPath' already exists"
            )

            assertThat(targetZipPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given target zip file path points to folder`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--output",
            "${tempFolder.path()}"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Capturing DPM sources",
                "DPM source recorder: ZIP file",
                "FATAL: Output file '${tempFolder.path()}' already exists"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--capture-dpm-sources-to-zip",
            "--output",
            "$targetZipPath"
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
            "--capture-dpm-sources-to-zip",
            "--output",
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
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}",
            "--output",
            "$targetZipPath"
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
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath",
            "--output",
            "$targetZipPath"
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
