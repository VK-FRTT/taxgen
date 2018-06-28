package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TestFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

@DisplayName("Executing CLI")
internal class TaxgenCli_Options_UnitTest {
    private lateinit var workFolderPath: Path

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun init() {
        workFolderPath = Files.createTempDirectory("taxgen_cli")

        charset = StandardCharsets.UTF_8
        outCollector = PrintStreamCollector(charset)
        errCollector = PrintStreamCollector(charset)

        cli = TaxgenCli(
            outStream = outCollector.printStream(),
            errStream = errCollector.printStream(),
            charset = charset,
            definedOptions = DefinedOptions()
        )
    }

    @AfterEach
    fun teardown() {
        Files
            .walk(workFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    @Test
    fun `Should use common exit code values`() {
        assertThat(TAXGEN_CLI_SUCCESS).isEqualTo(0)
        assertThat(TAXGEN_CLI_FAIL).isEqualTo(1)
    }

    @Test
    fun `Should fail when no options are given`() {
        val args = emptyArray<String>()
        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "No options given (-h will show valid options)"
        )
        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Test
    fun `Should fail when multiple commands with valid arguments are given`() {
        val args = arrayOf(
            "--produce-dpm-db",
            workFolderPath.resolve("output_dpm.db").toString(),
            "--capture-ycl-sources-to-folder",
            workFolderPath.resolve("output_ycl_capture").toString()
        )

        val (status, outText, errText) = executeCli(args)

        assertThat(errText).containsSubsequence(
            "yti-taxgen:",
            "Single command with proper argument must be given"
        )
        assertThat(outText).isBlank()
        assertThat(status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    @Nested
    @DisplayName("Command: help")
    inner class CommandHelp {

        @Test
        fun `Should list command line options`() {
            val args = arrayOf("--help")
            val (status, outText, errText) = executeCli(args)

            assertThat(errText).isBlank()

            assertThat(outText).containsSubsequence(
                "--help",
                "--produce-dpm-db",
                "--capture-ycl-sources-to-folder",
                "--capture-ycl-sources-to-zip",
                "--force-overwrite",
                "--source-config",
                "--source-folder",
                "--source-zip"
            )

            assertThat(status).isEqualTo(TAXGEN_CLI_SUCCESS)
        }
    }

    @Nested
    @DisplayName("Command: produce-dpm-db")
    inner class CommandProduceDpmDb {

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
        fun `Should overwrite target file when force option is given`() {
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
        fun `Should fail when target database is a folder`() {
            val sourceCapturePath = TestFixtures.yclSourceCapturePath("single_comprehensive_tree")

            val args = arrayOf(
                "--produce-dpm-db",
                workFolderPath.toString(),
                "--source-folder",
                sourceCapturePath.toString()
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

    private fun executeCli(args: Array<String>): ExecuteResult {
        val status = cli.execute(args)

        return ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )
    }

    private class PrintStreamCollector(val charset: Charset) {
        private val baos = ByteArrayOutputStream()
        private val ps = PrintStream(baos, true, charset.name())

        fun printStream(): PrintStream = ps

        fun grabText(): String {
            ps.close()
            return String(baos.toByteArray(), charset)
        }
    }

    private data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )
}
