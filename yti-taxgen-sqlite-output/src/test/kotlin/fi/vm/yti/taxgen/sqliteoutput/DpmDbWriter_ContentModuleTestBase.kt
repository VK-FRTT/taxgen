package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticPassAllFilteringPolicy
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.ExceptionHarness.withHaltExceptionHarness
import fi.vm.yti.taxgen.testcommons.TempFolder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.TestFactory

internal abstract class DpmDbWriter_ContentModuleTestBase {
    enum class DbInitMode {
        DICTIONARY_CREATE,
        DICTIONARY_REPLACE
    }

    protected lateinit var initMode: DbInitMode
    protected lateinit var tempFolder: TempFolder
    protected lateinit var outputDbPath: Path

    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticContext: DiagnosticBridge
    protected lateinit var dbConnection: Connection

    @AfterEach
    fun baseTeardown() {
        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }

        if (::tempFolder.isInitialized) {
            tempFolder.close()
        }
    }

    @TestFactory
    fun `When dictionary is created`(): List<DynamicNode> {
        initMode = DbInitMode.DICTIONARY_CREATE
        return createDynamicTests()
    }

    @TestFactory
    fun `When dictionary is replaced`(): List<DynamicNode> {
        initMode = DbInitMode.DICTIONARY_REPLACE
        return createDynamicTests()
    }

    abstract fun createDynamicTests(): List<DynamicNode>

    fun executeDpmDbWriterWithDefaults() {
        executeDpmDbWriter(
            exceptionIsExpected = false,
            diagnosticMessagesAreExpected = false,
            processingOptions = processingOptionsWithInherentTextLanguageFi(),
            varieties = *arrayOf(FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN)
        )
    }

    fun executeDpmDbWriter(
        exceptionIsExpected: Boolean,
        diagnosticMessagesAreExpected: Boolean,
        processingOptions: ProcessingOptions,
        vararg varieties: FixtureVariety
    ) {
        baseTeardown()

        tempFolder = TempFolder(javaClass.simpleName)
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(
            diagnosticCollector,
            DiagnosticHaltPolicy(),
            DiagnosticPassAllFilteringPolicy()
        )
        outputDbPath = tempFolder.resolve("${initMode.name.toLowerCase()}.db")

        withHaltExceptionHarness(diagnosticCollector, exceptionIsExpected) {

            val dpmDbWriter = when (initMode) {

                DbInitMode.DICTIONARY_CREATE -> {
                    SQLiteDpmDbWriterFactory.dictionaryCreateWriter(
                        outputDbPath,
                        false,
                        diagnosticContext
                    )
                }

                DbInitMode.DICTIONARY_REPLACE -> {

                    val baselineDbPath = tempFolder.resolve("baseline_plain_dictionary.db")

                    val stream = this::class.java.getResourceAsStream("/db_fixture/dpm_model_fixture_generated.db")
                    Files.copy(stream, baselineDbPath, StandardCopyOption.REPLACE_EXISTING)

                    SQLiteDpmDbWriterFactory.dictionaryReplaceWriter(
                        baselineDbPath = baselineDbPath,
                        outputDbPath = outputDbPath,
                        forceOverwrite = false,
                        diagnosticContext = diagnosticContext
                    )
                }
            }

            val model = dpmModelFixture(*varieties)
            dpmDbWriter.writeModel(model, processingOptions)

            dbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")
        }

        if (!diagnosticMessagesAreExpected) {
            assertThat(diagnosticCollector.allMessagesCount()).isEqualTo(0)
        }
    }

    fun processingOptionsWithInherentTextLanguageFi() = ProcessingOptions(
        diagnosticSourceLanguages = emptyList(),
        sqliteDbDpmElementInherentTextLanguage = Language.byIso6391CodeOrFail("fi"),
        sqliteDbMandatoryLabelLanguage = null,
        sqliteDbMandatoryLabelSourceLanguages = null,
        sqliteDbDpmElementUriStorageLabelLanguage = null,
        sqliteDbHierarchyNodeLabelCompositionLanguages = null,
        sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = null
    )
}
