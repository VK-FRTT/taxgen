package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.rdsprovider.SourceFactory
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_CAPTURE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal open class RdsToDpmMapper_UnitTestBase {

    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticContext: DiagnosticContext

    protected lateinit var en: Language
    protected lateinit var fi: Language
    protected lateinit var sv: Language

    @BeforeEach
    fun beforeEach() {
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)

        en = Language.findByIso6391Code("en")!!
        fi = Language.findByIso6391Code("fi")!!
        sv = Language.findByIso6391Code("sv")!!
    }

    @AfterEach
    fun afterEach() {
    }

    protected fun performMappingAndGetAllDictionaries(fixtureName: String): List<DpmDictionary> {
        val fixturePath = TestFixture.pathOf(RDS_CAPTURE, fixtureName)

        lateinit var model: DpmModel

        SourceFactory.sourceForFolder(fixturePath, diagnosticContext).use { sourceHolder ->
            sourceHolder.withDpmSource { dpmSource ->
                val mapper = RdsToDpmMapper(diagnosticContext)
                model = mapper.extractDpmModel(dpmSource)
            }
        }

        return model.dictionaries
    }

    protected fun performMappingFromIntegrationFixture(): DpmDictionary {
        val dictionary = performMappingAndGetAllDictionaries("dm_integration_fixture").first()

        val fails = diagnosticCollector.run { fatalCount + errorCount + validationCount }

        if (fails != 0) {
            println(diagnosticCollector.eventsString())
        }

        assertThat(diagnosticCollector.fatalCount).isEqualTo(0)
        assertThat(diagnosticCollector.errorCount).isEqualTo(0)
        assertThat(diagnosticCollector.validationCount).isEqualTo(0)

        return dictionary
    }
}
