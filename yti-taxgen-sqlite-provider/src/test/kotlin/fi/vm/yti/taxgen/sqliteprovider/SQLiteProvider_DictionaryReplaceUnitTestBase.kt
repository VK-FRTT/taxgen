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
    private lateinit var dbPath: Path
    protected lateinit var dbConnection: Connection
    protected lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_replace")
        dbPath = tempFolder.resolve("replace_dpm_dictionary.db")

        val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
        Files.copy(stream, dbPath, StandardCopyOption.REPLACE_EXISTING)

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        diagnosticCollector = DiagnosticCollector()
    }

    @AfterEach
    fun baseTeardown() {
        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }

        tempFolder.close()
    }

    protected fun replaceDictionaryInDb(variety: FixtureVariety = FixtureVariety.NONE) {
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)
        val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
            dbPath,
            diagnosticContext
        )

        val model = dpmModelFixture(variety)

        dbWriter.writeModel(model)
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
