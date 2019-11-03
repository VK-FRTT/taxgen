package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

internal open class DpmDbWriter_CreateFixtureDbTest {

    private lateinit var tempFolder: TempFolder

    private lateinit var outputDbPath: Path
    private lateinit var outputDbConnection: Connection

    private lateinit var diagnosticCollector: DiagnosticCollector
    private lateinit var diagnosticContext: DiagnosticBridge

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_created_fixture_db")
        outputDbPath = tempFolder.resolve("dpm_model_fixture_generated.db")

        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(diagnosticCollector, DiagnosticHaltPolicy())
    }

    @AfterEach
    fun baseTeardown() {
        if (::outputDbConnection.isInitialized) {
            outputDbConnection.close()
        }

        tempFolder.close()
    }

    @Test
    fun `Should produce fixture database`() {
        val dbWriter = DpmDbWriterFactory.dictionaryCreateWriter(
            outputDbPath,
            false,
            diagnosticContext
        )

        val model = dpmModelFixture(
            FixtureVariety.THREE_DISTINCT_DICTIONARIES,
            FixtureVariety.THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES
        )

        val processingOptions = ProcessingOptions(
            diagnosticSourceLanguages = emptyList(),
            sqliteDbDpmElementInherentTextLanguage = Language.findByIso6391Code("en")!!,
            sqliteDbMandatoryLabelLanguage = null,
            sqliteDbMandatoryLabelSourceLanguages = null,
            sqliteDbDpmElementUriStorageLabelLanguage = null,
            sqliteDbHierarchyNodeLabelCompositionLanguages = null,
            sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = null
        )

        dbWriter.writeModel(
            model,
            processingOptions
        )

        outputDbConnection = DriverManager.getConnection("jdbc:sqlite:$outputDbPath")

        Assertions.assertThat(diagnosticCollector.events).containsSequence(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )
    }
}
