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

    protected fun dpmSourceFolderAdapterToReferenceData(): Pair<DpmSourceFolderAdapter, Path> {
        val classLoader = Thread.currentThread().contextClassLoader
        val referenceUri = classLoader.getResource("folder_adapter_reference").toURI()
        val dpmSourceRootPath = Paths.get(referenceUri)
        return Pair(DpmSourceFolderAdapter(dpmSourceRootPath), dpmSourceRootPath)
    }

    protected fun extractMarkerValueFromJsonData(
        jsonDataProvider: () -> String
    ): String {
        val jsonData = jsonDataProvider()
        val json = objectMapper.readTree(jsonData)
        assertThat(json.isObject).isTrue()
        return json.get("marker").textValue()
    }

    protected fun extractMarkerValuesFromJsonData(
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

    protected fun assertMetricsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmMetric"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    protected fun assertExplicitDomainsAndHierarchiesBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmExplicitDomain"
        )

        assertThat(blueprint.usesSubCodeLists).isTrue()
        assertThat(blueprint.subCodeListBlueprint).isNotNull()

        assertThat(blueprint.subCodeListBlueprint!!.usesExtensions).isTrue()
        assertThat(blueprint.subCodeListBlueprint!!.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#definitionHierarchy",
            "http://uri.suomi.fi/datamodel/ns/code#calculationHierarchy"
        )

        assertThat(blueprint.subCodeListBlueprint!!.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint!!.subCodeListBlueprint).isNull()
    }

    protected fun assertTypedDomainBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmTypedDomain"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }

    protected fun assertExplictOrTypedDimensionsBlueprint(blueprint: CodeListBlueprint) {
        assertThat(blueprint.usesExtensions).isTrue()
        assertThat(blueprint.extensionPropertyTypeUris).containsExactly(
            "http://uri.suomi.fi/datamodel/ns/code#dpmDimension"
        )

        assertThat(blueprint.usesSubCodeLists).isFalse()
        assertThat(blueprint.subCodeListBlueprint).isNull()
    }
}
