package fi.vm.yti.taxgen.rdsprovider

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.folder.SourceProviderFolderAdapter
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import java.nio.file.Paths

open class DpmSource_UnitTestBase {

    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticContext: DiagnosticContext

    @BeforeEach
    fun baseInit() {
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)
    }

    @AfterEach
    fun baseTeardown() {
        diagnosticCollector.events.forEach {
            println(it)
        }
    }

    companion object {
        val objectMapper = jacksonObjectMapper()

        fun sourceProviderFolderAdapterFromReferenceData(
            diagnosticContext: DiagnosticContext? = null
        ): Pair<SourceProvider, Path> {

            val classLoader = Thread.currentThread().contextClassLoader
            val referenceUri = classLoader.getResource("folder_adapter_reference").toURI()
            val dpmSourceRootPath = Paths.get(referenceUri)

            val sourceProvider = if (diagnosticContext != null) {
                ProviderFactory.folderProvider(dpmSourceRootPath, diagnosticContext)
            } else {
                SourceProviderFolderAdapter(dpmSourceRootPath)
            }

            return Pair(sourceProvider, dpmSourceRootPath)
        }

        fun extractMarkerValueFromJsonData(
            jsonDataProvider: () -> String
        ): String {
            val jsonData = jsonDataProvider()
            val json = objectMapper.readTree(jsonData)
            assertThat(json.isObject).isTrue()
            return json.get("marker").textValue()
        }

        fun extractMarkerValueFromJsonData(
            markers: MutableList<String>,
            jsonData: String
        ) {
            val json = objectMapper.readTree(jsonData)
            assertThat(json.isObject).isTrue()
            markers.add(json.get("marker").textValue())
        }

        fun <T> extractMarkerValuesFromJsonData(
            objects: List<T>,
            jsonDataExtractor: (T) -> String
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
