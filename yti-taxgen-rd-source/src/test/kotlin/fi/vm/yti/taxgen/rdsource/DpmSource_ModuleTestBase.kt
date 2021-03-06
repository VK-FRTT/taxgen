package fi.vm.yti.taxgen.rdsource

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticHaltPolicy
import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticPassAllFilteringPolicy
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.diagnostic.system.DiagnosticBridge
import fi.vm.yti.taxgen.rdsource.contextdiagnostic.SourceHolderContextDecorator
import fi.vm.yti.taxgen.rdsource.folder.SourceHolderFolderAdapter
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class DpmSource_ModuleTestBase {

    protected lateinit var diagnosticCollector: DiagnosticCollector
    protected lateinit var diagnosticContext: DiagnosticContext

    @BeforeEach
    fun baseInit() {
        diagnosticCollector = DiagnosticCollector()
        diagnosticContext = DiagnosticBridge(
            diagnosticCollector,
            DiagnosticHaltPolicy(),
            DiagnosticPassAllFilteringPolicy()
        )
    }

    @AfterEach
    fun baseTeardown() {
    }

    companion object {
        val objectMapper = jacksonObjectMapper()

        fun sourceHolderFolderAdapterForBundledReferenceData(
            diagnosticContext: DiagnosticContext,
            contextDecorateSource: Boolean
        ): Pair<SourceHolder, Path> {

            val classLoader = Thread.currentThread().contextClassLoader
            val referenceUri = classLoader.getResource("folder_adapter_reference").toURI()
            val dpmSourceRootPath = Paths.get(referenceUri)

            val sourceHolder = SourceHolderFolderAdapter(
                dpmSourceRootPath = dpmSourceRootPath,
                diagnosticContext = diagnosticContext
            ).let {
                if (contextDecorateSource) {
                    SourceHolderContextDecorator(
                        realSourceHolder = it,
                        diagnosticContext = diagnosticContext
                    )
                } else {
                    it
                }
            }

            return Pair(sourceHolder, dpmSourceRootPath)
        }
    }
}
