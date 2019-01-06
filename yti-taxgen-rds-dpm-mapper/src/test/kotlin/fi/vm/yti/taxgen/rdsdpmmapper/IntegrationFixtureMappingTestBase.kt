package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.rdsprovider.ProviderFactory
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_CAPTURE
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal open class IntegrationFixtureMappingTestBase {

    protected lateinit var diagnosticCollector: DiagnosticCollectorSimple
    protected lateinit var diagnosticContext: DiagnosticContext

    protected lateinit var en: Language
    protected lateinit var fi: Language
    protected lateinit var sv: Language

    @BeforeEach
    fun beforeEach() {
        diagnosticCollector = DiagnosticCollectorSimple()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)

        en = Language.findByIso6391Code("en")!!
        fi = Language.findByIso6391Code("fi")!!
        sv = Language.findByIso6391Code("sv")!!
    }

    @AfterEach
    fun afterEach() {
    }

    protected fun performMappingAndGetAll(): List<DpmDictionary> {
        val fixturePath = TestFixture.pathOf(RDS_CAPTURE, "dm_integration_fixture")
        val sourceProvider = ProviderFactory.folderProvider(fixturePath, diagnosticContext)
        val mapper = RdsToDpmMapper(diagnosticContext)

        val dictionaries = mapper.extractDpmDictionariesFromSource(sourceProvider)

        sourceProvider.close()

        return dictionaries
    }

    protected fun performMappingAndGetFirst(): DpmDictionary {
        return performMappingAndGetAll().first()
    }
}
