package fi.vm.yti.taxgen.rdsprovider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import java.nio.file.Paths

open class DpmSource_UnitTestBase {

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

    companion object {
        val objectMapper = jacksonObjectMapper()

        fun dpmSourceFolderAdapterToReferenceData(): Pair<DpmSourceFolderAdapter, Path> {
            val classLoader = Thread.currentThread().contextClassLoader
            val referenceUri = classLoader.getResource("folder_adapter_reference").toURI()
            val dpmSourceRootPath = Paths.get(referenceUri)
            return Pair(DpmSourceFolderAdapter(dpmSourceRootPath), dpmSourceRootPath)
        }

        fun extractMarkerValueFromJsonData(
            jsonDataProvider: () -> String
        ): String {
            val jsonData = jsonDataProvider()
            val json = objectMapper.readTree(jsonData)
            assertThat(json.isObject).isTrue()
            return json.get("marker").textValue()
        }

        fun extractMarkerValuesFromJsonData(
            objects: Sequence<Any>,
            jsonDataExtractor: (Any) -> String
        ): List<String> {
            val markers = objects.map { obj ->
                val jsonData = jsonDataExtractor(obj)
                val json = objectMapper.readTree(jsonData)
                assertThat(json.isObject).isTrue()

                json.get("marker").textValue()
            }

            return markers.toList()
        }
    }
}
