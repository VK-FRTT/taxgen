package fi.vm.yti.taxgen.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

@DisplayName("Command line interface")
internal class TaxgenCli_UnitTest {

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun init() {
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

    @Test
    fun `Should provide help text about command line options`() {
        val status = cli.execute(arrayOf("--help"))

        val outText = outCollector.grabText()
        val errText = errCollector.grabText()

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

        assertThat(status).isEqualTo(0)
    }

    class PrintStreamCollector(val charset: Charset) {
        private val baos = ByteArrayOutputStream()
        private val ps = PrintStream(baos, true, charset.name())

        fun printStream(): PrintStream = ps

        fun grabText(): String {
            ps.close()
            return String(baos.toByteArray(), charset)
        }
    }
}
