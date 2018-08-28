package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.TestFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path

open class TaxgenCli_UnitTestBase(val primaryCommand: String? = null) {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var yclSourceCapturePath: Path
    protected lateinit var yclSourceConfigPath: Path

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("taxgen_cli")

        yclSourceCapturePath = tempFolder.copyFolderRecursivelyUnderSubfolder(
            TestFixture.yclSourceCapturePath("single_comprehensive_tree"),
            "source_capture"
        )

        yclSourceConfigPath = tempFolder.copyFileToSubfolder(
            TestFixture.yclSourceConfigPath("single_comprehensive_tree"),
            "source_config"
        )

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
    fun baseTeardown() {
        tempFolder.close()
    }

    protected fun executeCli(args: Array<String>): ExecuteResult {
        if (primaryCommand != null) {
            assertThat(args).contains(primaryCommand)
        }

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

    protected data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )
}
