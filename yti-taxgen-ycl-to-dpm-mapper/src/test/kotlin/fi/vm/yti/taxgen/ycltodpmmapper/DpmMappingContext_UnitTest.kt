package fi.vm.yti.taxgen.ycltodpmmapper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextProvider
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.testcommons.DiagnosticConsumerCaptor
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
        override fun contextName() = "name-$discriminator"
        override fun contextRef() = "ref-$discriminator"
    }

    private lateinit var diagnosticConsumerCaptor: DiagnosticConsumerCaptor
    private lateinit var diagnostic: Diagnostic
    private lateinit var extractValue: Validatable
    private lateinit var extractValue2: Validatable

    @BeforeEach
    fun init() {
        diagnosticConsumerCaptor = DiagnosticConsumerCaptor()
        diagnostic = DiagnosticBridge(diagnosticConsumerCaptor)
        extractValue = mock(Validatable::class.java)
        extractValue2 = mock(Validatable::class.java)
    }

    @Test
    fun `Single extract reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticContextProvider(
            DiagnosticContextType.YclCode,
            "A")) {
            extractValue
        }

        verify(extractRetValue, times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticConsumerCaptor.events).containsExactly(
            "ENTER [CTX{YclCode,name-A,ref-A}]",
            "EXIT [] RETIRED [CTX{YclCode,name-A,ref-A}]"
        )
    }

    @Test
    fun `Nested extracts report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.YclCode, "A")) {
            ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.YclSource, "B")) {
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticConsumerCaptor.events).containsExactly(
            "ENTER [CTX{YclCode,name-A,ref-A}]",
            "ENTER [CTX{YclSource,name-B,ref-B}, CTX{YclCode,name-A,ref-A}]",
            "EXIT [CTX{YclCode,name-A,ref-A}] RETIRED [CTX{YclSource,name-B,ref-B}]",
            "EXIT [] RETIRED [CTX{YclCode,name-A,ref-A}]"
        )
    }

    @Test
    fun `Nested extracts with diagnostic context name updates report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.YclCode, "A")) {
            ctx.diagnostic.updateCurrentContextName("updated-name-A")

            ctx.extract(FixedDiagnosticContextProvider(DiagnosticContextType.YclSource, "B")) {
                ctx.diagnostic.updateCurrentContextName("updated-name-B")
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticConsumerCaptor.events).containsExactly(
            "ENTER [CTX{YclCode,name-A,ref-A}]",
            "UPDATE [CTX{YclCode,updated-name-A,ref-A}] ORIGINAL [CTX{YclCode,name-A,ref-A}]",
            "ENTER [CTX{YclSource,name-B,ref-B}, CTX{YclCode,updated-name-A,ref-A}]",
            "UPDATE [CTX{YclSource,updated-name-B,ref-B}, CTX{YclCode,updated-name-A,ref-A}] ORIGINAL [CTX{YclSource,name-B,ref-B}]",
            "EXIT [CTX{YclCode,updated-name-A,ref-A}] RETIRED [CTX{YclSource,updated-name-B,ref-B}]",
            "EXIT [] RETIRED [CTX{YclCode,updated-name-A,ref-A}]"
        )
    }

    @Test
    fun `Single extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.YclCode, "A")) {
            listOf(extractValue, extractValue2)
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticConsumerCaptor.events).containsExactly(
            "ENTER [CTX{YclCode,name-A,ref-A}]",
            "EXIT [] RETIRED [CTX{YclCode,name-A,ref-A}]"
        )
    }

    @Test
    fun `Nested extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.YclCode, "A")) {
            ctx.extractList(FixedDiagnosticContextProvider(DiagnosticContextType.YclSource, "B")) {
                listOf(extractValue, extractValue2)
            }
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticConsumerCaptor.events).containsExactly(
            "ENTER [CTX{YclCode,name-A,ref-A}]",
            "ENTER [CTX{YclSource,name-B,ref-B}, CTX{YclCode,name-A,ref-A}]",
            "EXIT [CTX{YclCode,name-A,ref-A}] RETIRED [CTX{YclSource,name-B,ref-B}]",
            "EXIT [] RETIRED [CTX{YclCode,name-A,ref-A}]"
        )
    }
}
