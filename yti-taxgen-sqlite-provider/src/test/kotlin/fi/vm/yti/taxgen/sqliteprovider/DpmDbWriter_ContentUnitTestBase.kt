package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager

internal abstract class DpmDbWriter_ContentUnitTestBase {
    enum class DbInitMode {
        DICTIONARY_CREATE,
        DICTIONARY_REPLACE
    }

    data class TestContext(
        val mode: DbInitMode,
        val dbWriteException: Throwable?,
        val dbConnection: Connection,
        val diagnosticCollector: DiagnosticCollector
    )

    private lateinit var tempFolder: TempFolder
    private lateinit var dictionaryCreateDbConnection: Connection
    private lateinit var dictionaryReplaceDbConnection: Connection

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_content")
    }

    @AfterEach
    fun baseTeardown() {
        if (::dictionaryCreateDbConnection.isInitialized) {
            dictionaryCreateDbConnection.close()
        }

        if (::dictionaryReplaceDbConnection.isInitialized) {
            dictionaryReplaceDbConnection.close()
        }

        tempFolder.close()
    }

    @TestFactory
    fun `When dictionary is created`(): List<DynamicNode> {
        return createDynamicTests(initDbViaDictionaryCreate())
    }

    @TestFactory
    fun `When dictionary is replaced`(): List<DynamicNode> {
        return createDynamicTests(initDbViaDictionaryReplace())
    }

    abstract fun createDynamicTests(ctx: TestContext): List<DynamicNode>

    fun initDbViaDictionaryCreate(variety: FixtureVariety = FixtureVariety.NONE): TestContext {
        val dbPath = tempFolder.resolve("created_dpm_dictionary.db")

        val diagnosticCollector = DiagnosticCollector()
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)

        val model = DpmModel(
            dictionaries = listOf(dpmDictionaryFixture(variety))
        )

        val dbWriteException = catchException {
            val dbWriter = DpmDbWriterFactory.dictionaryCreateWriter(
                dbPath,
                false,
                diagnosticContext
            )

            dbWriter.writeModel(model)
        }

        dictionaryCreateDbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        return TestContext(
            mode = DbInitMode.DICTIONARY_CREATE,
            dbWriteException = dbWriteException,
            dbConnection = dictionaryCreateDbConnection,
            diagnosticCollector = diagnosticCollector
        )
    }

    fun initDbViaDictionaryReplace(): TestContext {
        val dbPath = tempFolder.resolve("replaced_dpm_dictionary.db")

        val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
        Files.copy(stream, dbPath, StandardCopyOption.REPLACE_EXISTING)

        val diagnosticCollector = DiagnosticCollector()
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)

        val model = DpmModel(
            dictionaries = listOf(dpmDictionaryFixture(FixtureVariety.NONE))
        )

        val dbWriteException = catchException {
            val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
                dbPath,
                diagnosticContext
            )

            dbWriter.writeModel(model)
        }

        dictionaryReplaceDbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        return TestContext(
            mode = DbInitMode.DICTIONARY_REPLACE,
            dbWriteException = dbWriteException,
            dbConnection = dictionaryReplaceDbConnection,
            diagnosticCollector = diagnosticCollector
        )
    }

    private fun catchException(action: () -> Unit): Throwable? {
        try {
            action()
        } catch (throwable: Throwable) {
            return throwable
        }

        return null
    }
}
