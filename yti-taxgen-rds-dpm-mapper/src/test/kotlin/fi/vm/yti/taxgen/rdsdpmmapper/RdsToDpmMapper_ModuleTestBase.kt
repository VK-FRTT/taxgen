package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.rdsprovider.SourceFactory
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_CAPTURE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

internal open class RdsToDpmMapper_ModuleTestBase {

    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticBridge: DiagnosticBridge

    protected lateinit var en: Language
    protected lateinit var fi: Language
    protected lateinit var sv: Language

    @BeforeEach
    fun beforeEach() {
        diagnosticCollector = DiagnosticCollector()
        diagnosticBridge = DiagnosticBridge(
            diagnosticCollector, DiagnosticHaltPolicy()
        )

        en = Language.findByIso6391Code("en")!!
        fi = Language.findByIso6391Code("fi")!!
        sv = Language.findByIso6391Code("sv")!!

        diagnosticBridge.setDiagnosticSourceLanguages(
            listOf(fi)
        )
    }

    @AfterEach
    fun afterEach() {
    }

    protected fun executeRdsToDpmMapperAndGetDictionariesFrom(fixtureName: String): List<DpmDictionary> {
        val fixturePath = TestFixture.sourcePathOf(RDS_CAPTURE, fixtureName)

        lateinit var model: DpmModel

        SourceFactory.sourceForFolder(fixturePath, diagnosticBridge).use { sourceHolder ->
            sourceHolder.withDpmSource { dpmSource ->

                dpmSource.config().processingOptions

                val mapper = RdsToDpmMapper(diagnosticBridge)
                model = mapper.extractDpmModel(dpmSource)
            }
        }

        return model.dictionaries
    }

    protected fun executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture(): DpmDictionary {
        val dictionary = executeRdsToDpmMapperAndGetDictionariesFrom("integration_fixture").first()

        val count = diagnosticCollector.criticalMessagesCount() + diagnosticCollector.objectValidationFailureCount()

        if (count != 0) {
            println(diagnosticCollector.eventsString())
        }

        assertThat(count).isEqualTo(0)

        return dictionary
    }
}
