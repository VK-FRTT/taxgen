package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
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

    @TestFactory
    fun `When dictionary is created`(): List<DynamicNode> {
        setupDbViaDictionaryCreate()

        return createDynamicTests()
    }

    @TestFactory
    fun `When dictionary is replaced`(): List<DynamicNode> {
        setupDbViaDictionaryReplace()

        return createDynamicTests()
    }

    abstract fun createDynamicTests(): List<DynamicNode>

    fun setupDbViaDictionaryCreate(variety: FixtureVariety = FixtureVariety.NONE) {
        initMode = DbInitMode.DICTIONARY_CREATE
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)

        val dbPath = tempFolder.resolve("created_dpm_dictionary.db")
        val model = dpmModelFixture(variety)

        val dbWriter = DpmDbWriterFactory.dictionaryCreateWriter(
            dbPath,
            false,
            diagnosticContext
        )

        val thrown = catchThrowable { dbWriter.writeModel(model) }

        if (thrown is HaltException) {
            println(diagnosticCollector.eventsString())
        }

        if (thrown != null) {
            throw thrown
        }

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    fun setupDbViaDictionaryReplace(variety: FixtureVariety = FixtureVariety.NONE) {
        initMode = DbInitMode.DICTIONARY_REPLACE
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)

        val baselineDbPath = tempFolder.resolve("baseline_plain_dictionary.db")
        val outputDbPath = tempFolder.resolve("replaced_dpm_dictionary.db")

        val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
        Files.copy(stream, baselineDbPath, StandardCopyOption.REPLACE_EXISTING)

        val model = dpmModelFixture(variety)

        val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
            baselineDbPath = baselineDbPath,
            outputDbPath = outputDbPath,
            forceOverwrite = false,
            diagnosticContext = diagnosticContext
        )

        val thrown = catchThrowable { dbWriter.writeModel(model) }

        if (thrown is HaltException) {
            println(diagnosticCollector.eventsString())
        }

        if (thrown != null) {
            throw thrown
        }

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")
    }
}
