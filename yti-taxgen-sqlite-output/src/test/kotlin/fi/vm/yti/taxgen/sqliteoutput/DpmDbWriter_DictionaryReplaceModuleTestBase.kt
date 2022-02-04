package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticPassAllFilteringPolicy
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.ExceptionHarness
import fi.vm.yti.taxgen.testcommons.TempFolder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal open class DpmDbWriter_DictionaryReplaceModuleTestBase {

    private lateinit var tempFolder: TempFolder

    private lateinit var baselineDbPath: Path
    protected lateinit var baselineDbConnection: Connection

    private lateinit var outputDbPath: Path
    protected lateinit var outputDbConnection: Connection

    protected lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_replace")
        baselineDbPath = tempFolder.resolve("baseline_plain_dictionary.db")
        outputDbPath = tempFolder.resolve("replace_dpm_dictionary.db")

        val stream = this::class.java.getResourceAsStream("/db_fixture/dpm_model_fixture_generated.db")
        Files.copy(stream, baselineDbPath, StandardCopyOption.REPLACE_EXISTING)

        baselineDbConnection = DriverManager.getConnection("jdbc:sqlite:$baselineDbPath")

        diagnosticCollector = DiagnosticCollector()
    }

    @AfterEach
    fun baseTeardown() {
        if (::baselineDbConnection.isInitialized) {
            baselineDbConnection.close()
        }

        if (::outputDbConnection.isInitialized) {
            outputDbConnection.close()
        }

        tempFolder.close()
    }

    protected fun replaceDictionaryInDb(
        vararg varieties: FixtureVariety = arrayOf(FixtureVariety.NONE),
        exceptionIsExpected: Boolean = false
    ) {
        ExceptionHarness.withHaltExceptionHarness(
            diagnosticCollector = diagnosticCollector,
            exceptionIsExpected = exceptionIsExpected
        ) {
            val diagnosticContext =
                DiagnosticBridge(
                    diagnosticCollector,
                    DiagnosticHaltPolicy(),
                    DiagnosticPassAllFilteringPolicy()
                )

            val dbWriter = SQLiteDpmDbWriterFactory.dictionaryReplaceWriter(
                baselineDbPath = baselineDbPath,
                outputDbPath = outputDbPath,
                forceOverwrite = true,
                keepPartialOutput = true,
                diagnosticContext = diagnosticContext
            )

            val model = dpmModelFixture(
                *varieties
            )

            val processingOptions =
                ProcessingOptions(emptyList(), null, null, null, null, null, null)

            dbWriter.writeModel(
                model,
                processingOptions
            )

            outputDbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")
        }
    }

    protected fun ensureHaltThrown(action: () -> Unit) {
        val thrown = catchThrowable { action() }
        assertThat(thrown).isInstanceOf(HaltException::class.java)
    }
}
