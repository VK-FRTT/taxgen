package fi.vm.yti.taxgen.cli

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.YCL_SOURCE_CAPTURE
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.YCL_SOURCE_CONFIG
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

open class TaxgenCli_UnitTestBase(val primaryCommand: String? = null) {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var yclSourceCapturePath: String
    protected lateinit var yclSourceConfigPath: String

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("taxgen_cli")

        yclSourceCapturePath = tempTestFixture(YCL_SOURCE_CAPTURE, "codelist_comprehensive")
        yclSourceConfigPath = tempTestFixture(YCL_SOURCE_CONFIG, "codelist_comprehensive.json")

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

            YCL_SOURCE_CAPTURE -> tempFolder.copyFolderRecursivelyUnderSubfolder(
                fixturePath,
                fixtureType.folderName
            ).toString()

            YCL_SOURCE_CONFIG -> tempFolder.copyFileToSubfolder(
                fixturePath,
                fixtureType.folderName
            ).toString()

            else -> thisShouldNeverHappen("Unsupported fixture type")
        }
    }

    protected fun executeCli(args: Array<String>): ExecuteResult {
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
