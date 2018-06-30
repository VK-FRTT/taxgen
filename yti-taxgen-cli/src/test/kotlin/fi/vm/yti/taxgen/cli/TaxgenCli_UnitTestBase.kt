package fi.vm.yti.taxgen.cli

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator

open class TaxgenCli_UnitTestBase {
    protected lateinit var workFolderPath: Path

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun baseInit() {
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
    fun baseTeardown() {
        Files
            .walk(workFolderPath)
            .sorted(Comparator.reverseOrder())
            .forEach { Files.deleteIfExists(it) }
    }

    protected fun executeCli(args: Array<String>): ExecuteResult {
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
