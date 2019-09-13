package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.dpmmodel.DpmModelOption
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager

internal abstract class SQLiteProvider_ContentUnitTestBase {
    enum class DbInitMode {
        DICTIONARY_CREATE,
        DICTIONARY_REPLACE
    }

    protected lateinit var initMode: DbInitMode
    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticContext: DiagnosticBridge
    protected lateinit var dbConnection: Connection

    private lateinit var tempFolder: TempFolder

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder(javaClass.simpleName)
    }

    @AfterEach
    fun baseTeardown() {
        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }

        tempFolder.close()
    }

    fun setupDbViaDictionaryCreate(
        exceptionIsExpected: Boolean,
        variety: FixtureVariety,
        modelOptions: Map<DpmModelOption, Any>
    ) {
        withHaltExceptionHarness(exceptionIsExpected) {
            initMode = DbInitMode.DICTIONARY_CREATE
            diagnosticCollector = DiagnosticCollector()
            diagnosticContext = DiagnosticBridge(diagnosticCollector)

            val dbPath = tempFolder.resolve("created_dpm_dictionary.db")
            val model = dpmModelFixture(variety, modelOptions)

            val dbWriter = DpmDbWriterFactory.dictionaryCreateWriter(
                dbPath,
                false,
                diagnosticContext
            )

            dbWriter.writeModel(model)

            dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
        }
    }

    fun setupDbViaDictionaryReplace(
        exceptionIsExpected: Boolean,
        variety: FixtureVariety = FixtureVariety.NONE,
        modelOptions: Map<DpmModelOption, Any>
    ) {
        withHaltExceptionHarness(exceptionIsExpected) {
            initMode = DbInitMode.DICTIONARY_REPLACE
            diagnosticCollector = DiagnosticCollector()
            diagnosticContext = DiagnosticBridge(diagnosticCollector)

            val baselineDbPath = tempFolder.resolve("baseline_plain_dictionary.db")
            val outputDbPath = tempFolder.resolve("replaced_dpm_dictionary.db")

            val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
            Files.copy(stream, baselineDbPath, StandardCopyOption.REPLACE_EXISTING)

            val model = dpmModelFixture(variety, modelOptions)

            val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
                baselineDbPath = baselineDbPath,
                outputDbPath = outputDbPath,
                forceOverwrite = false,
                diagnosticContext = diagnosticContext
            )

            dbWriter.writeModel(model)
            dbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")
        }
    }

    private fun withHaltExceptionHarness(
        exceptionIsExpected: Boolean,
        action: () -> Unit
    ) {
        return try {
            action()
        } catch (exception: HaltException) {
            if (!exceptionIsExpected) {
                println(diagnosticCollector.eventsString())
            }

            throw exception
        }
    }
}
