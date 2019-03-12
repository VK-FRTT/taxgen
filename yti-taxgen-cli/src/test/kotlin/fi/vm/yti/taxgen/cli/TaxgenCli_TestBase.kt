package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_CAPTURE
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_SOURCE_CONFIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

open class TaxgenCli_TestBase(val primaryCommand: String? = null) {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var dpmSourceCapturePath: String
    protected lateinit var dpmSourceConfigPath: String

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("taxgen_cli")

        dpmSourceCapturePath = tempTestFixture(RDS_CAPTURE, "dm_integration_fixture")
        dpmSourceConfigPath = tempTestFixture(RDS_SOURCE_CONFIG, "dm_integration_fixture.json")

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

    protected fun tempTestFixture(
        fixtureType: TestFixture.Type,
        fixtureName: String
    ): String {

        val fixturePath = TestFixture.pathOf(fixtureType, fixtureName)

        return when (fixtureType) {

            RDS_CAPTURE -> tempFolder.copyFolderRecursivelyUnderSubfolder(
                fixturePath,
                fixtureType.folderName
            ).toString()

            RDS_SOURCE_CONFIG -> tempFolder.copyFileToSubfolder(
                fixturePath,
                fixtureType.folderName
            ).toString()

            else -> thisShouldNeverHappen("Unsupported fixture type")
        }
    }

    private fun executeCli(args: Array<String>): ExecuteResult {
        if (primaryCommand != null) {
            assertThat(args).contains(primaryCommand)
        }

        val status = cli.execute(args)

        val result = ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )

        //println("OUT >>>\n${result.outText}\n<<< OUT")
        //println("ERR >>>\n${result.errText}\n<<< ERR")

        return result
    }

    protected fun executeCliAndExpectSuccess(args: Array<String>, verifier: (String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isBlank()

        verifier(result.outText)

        assertThat(result.status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    protected fun executeCliAndExpectFail(args: Array<String>, verifier: (String, String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isNotBlank()

        verifier(result.outText, result.errText)

        assertThat(result.status).isEqualTo(TAXGEN_CLI_FAIL)
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
