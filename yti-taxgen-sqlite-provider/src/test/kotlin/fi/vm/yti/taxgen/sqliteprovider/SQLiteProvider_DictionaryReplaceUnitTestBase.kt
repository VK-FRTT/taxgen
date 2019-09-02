package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager

internal open class SQLiteProvider_DictionaryReplaceUnitTestBase {

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

        val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
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

    protected fun replaceDictionaryInDb(variety: FixtureVariety = FixtureVariety.NONE) {
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)
        val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
            baselineDbPath = baselineDbPath,
            outputDbPath = outputDbPath,
            forceOverwrite = true,
            diagnosticContext = diagnosticContext
        )

        val model = dpmModelFixture(variety)

        dbWriter.writeModel(model)

        outputDbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")
    }

    protected fun dumpDiagnosticsWhenThrown(action: () -> Unit) {

        val thrown = catchThrowable { action() }

        if (thrown != null) {
            println(diagnosticCollector.events)
            throw thrown
        }
    }

    protected fun ensureHaltThrown(action: () -> Unit) {
        val thrown = catchThrowable { action() }
        assertThat(thrown).isInstanceOf(HaltException::class.java)
    }
}
