package fi.vm.yti.taxgen.rdsdpmmapper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class DpmMappingContext_UnitTest {

    class FixedDiagnosticContextProvider(
        private val ctxType: DiagnosticContextType,
        private val discriminator: String
    ) : DiagnosticContextProvider {
        override fun contextType() = ctxType
        override fun contextLabel() = "label-$discriminator"
        override fun contextIdentifier() = "id-$discriminator"
    }

    private lateinit var diagnosticCollector: DiagnosticCollector
    private lateinit var diagnostic: Diagnostic
    private lateinit var extractValue: Validatable
    private lateinit var extractValue2: Validatable

    @BeforeEach
    fun init() {
        diagnosticCollector = DiagnosticCollector()
        diagnostic = DiagnosticBridge(diagnosticCollector)
        extractValue = mock(Validatable::class.java)
        extractValue2 = mock(Validatable::class.java)
    }

    @Test
    fun `Single extract reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(
            FixedDiagnosticContextProvider(
                DiagnosticContextType.RdsCode,
                "A"
            )
        ) {
            extractValue
        }

        verify(extractRetValue, times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [CTX{RdsCode,label-A,id-A}]",
            "EXIT [] RETIRED [CTX{RdsCode,label-A,id-A}]"
        )
    }

    @Test
    fun `Nested extracts report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.RdsCode, "A")) {
            ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.DpmSource, "B")) {
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [CTX{RdsCode,label-A,id-A}]",
            "ENTER [CTX{DpmSource,label-B,id-B}, CTX{RdsCode,label-A,id-A}]",
            "EXIT [CTX{RdsCode,label-A,id-A}] RETIRED [CTX{DpmSource,label-B,id-B}]",
            "EXIT [] RETIRED [CTX{RdsCode,label-A,id-A}]"
        )
    }

    @Test
    fun `Nested extracts with diagnostic context name updates report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.RdsCode, "A")) {
            ctx.diagnostic.updateCurrentContextDetails(label = "updated-label-A")

            ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.DpmSource, "B")) {
                ctx.diagnostic.updateCurrentContextDetails(label = "updated-label-B")
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [CTX{RdsCode,label-A,id-A}]",
            "UPDATE [CTX{RdsCode,updated-label-A,id-A}] ORIGINAL [CTX{RdsCode,label-A,id-A}]",
            "ENTER [CTX{DpmSource,label-B,id-B}, CTX{RdsCode,updated-label-A,id-A}]",
            "UPDATE [CTX{DpmSource,updated-label-B,id-B}, CTX{RdsCode,updated-label-A,id-A}] ORIGINAL [CTX{DpmSource,label-B,id-B}]",
            "EXIT [CTX{RdsCode,updated-label-A,id-A}] RETIRED [CTX{DpmSource,updated-label-B,id-B}]",
            "EXIT [] RETIRED [CTX{RdsCode,updated-label-A,id-A}]"
        )
    }

    @Test
    fun `Single extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.RdsCode, "A")) {
            listOf(extractValue, extractValue2)
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [CTX{RdsCode,label-A,id-A}]",
            "EXIT [] RETIRED [CTX{RdsCode,label-A,id-A}]"
        )
    }

    @Test
    fun `Nested extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.RdsCode, "A")) {
            ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.DpmSource, "B")) {
                listOf(extractValue, extractValue2)
            }
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [CTX{RdsCode,label-A,id-A}]",
            "ENTER [CTX{DpmSource,label-B,id-B}, CTX{RdsCode,label-A,id-A}]",
            "EXIT [CTX{RdsCode,label-A,id-A}] RETIRED [CTX{DpmSource,label-B,id-B}]",
            "EXIT [] RETIRED [CTX{RdsCode,label-A,id-A}]"
        )
    }
}
