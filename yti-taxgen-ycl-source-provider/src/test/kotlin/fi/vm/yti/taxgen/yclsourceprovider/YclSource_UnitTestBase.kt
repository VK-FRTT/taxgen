package fi.vm.yti.taxgen.yclsourceprovider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class YclSource_UnitTestBase {

    protected val objectMapper = jacksonObjectMapper()

    protected lateinit var diagnosticCollector: DiagnosticCollectorSimple
    protected lateinit var diagnostic: Diagnostic

    @BeforeEach
    fun baseInit() {
        diagnosticCollector = DiagnosticCollectorSimple()
        diagnostic = DiagnosticBridge(diagnosticCollector)
    }

    @AfterEach
    fun baseTeardown() {
        diagnosticCollector.events.forEach {
            println(it)
        }
    }

    protected fun extractMarkerValuesFromJsonData(
        objects: List<Any>,
        jsonDataExtractor: (Any) -> String
    ): List<String> {
        val markers = objects.map { obj ->
            val jsonData = jsonDataExtractor(obj)
            val json = objectMapper.readTree(jsonData)
            assertThat(json.isObject).isTrue()

            json.get("marker").textValue()
        }

        return markers
    }
}
