package fi.vm.yti.taxgen.ycltodpmmapper

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticTopicProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock

class DpmMappingContext_UnitTest {

    class FixedDiagnosticTopicProvider(private val discriminator: String) : DiagnosticTopicProvider {
        override fun topicType() = "type-$discriminator"
        override fun topicName() = "name-$discriminator"
        override fun topicIdentifier() = "identifier-$discriminator"
    }

    private lateinit var diagnosticCaptor: DiagnosticCaptorDetailed
    private lateinit var diagnostic: Diagnostic
    private lateinit var extractValue: Validatable
    private lateinit var extractValue2: Validatable

    @BeforeEach
    fun init() {
        diagnosticCaptor = DiagnosticCaptorDetailed()
        diagnostic = Diagnostic(diagnosticCaptor)
        extractValue = mock(Validatable::class.java)
        extractValue2 = mock(Validatable::class.java)
    }

    @Test
    fun `Single extract reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticTopicProvider("A")) {
            extractValue
        }

        verify(extractRetValue, times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCaptor.events).containsExactly(
            "ENTER [TOPIC{type-A,name-A,identifier-A}]",
            "EXIT [] RETIRED [TOPIC{type-A,name-A,identifier-A}]"
        )
    }

    @Test
    fun `Nested extracts report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticTopicProvider("A")) {
            ctx.extract(FixedDiagnosticTopicProvider("B")) {
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCaptor.events).containsExactly(
            "ENTER [TOPIC{type-A,name-A,identifier-A}]",
            "ENTER [TOPIC{type-B,name-B,identifier-B}, TOPIC{type-A,name-A,identifier-A}]",
            "EXIT [TOPIC{type-A,name-A,identifier-A}] RETIRED [TOPIC{type-B,name-B,identifier-B}]",
            "EXIT [] RETIRED [TOPIC{type-A,name-A,identifier-A}]"
        )
    }

    @Test
    fun `Nested extracts with diagnostic topic name updates report proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extract(FixedDiagnosticTopicProvider("A")) {
            ctx.diagnostic.updateCurrentTopicName("updated-name-A")

            ctx.extract(FixedDiagnosticTopicProvider("B")) {
                ctx.diagnostic.updateCurrentTopicName("updated-name-B")
                extractValue
            }
        }

        verify(extractRetValue, times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue)

        assertThat(diagnosticCaptor.events).containsExactly(
            "ENTER [TOPIC{type-A,name-A,identifier-A}]",
            "UPDATE [TOPIC{type-A,updated-name-A,identifier-A}] ORIGINAL [TOPIC{type-A,name-A,identifier-A}]",
            "ENTER [TOPIC{type-B,name-B,identifier-B}, TOPIC{type-A,updated-name-A,identifier-A}]",
            "UPDATE [TOPIC{type-B,updated-name-B,identifier-B}, TOPIC{type-A,updated-name-A,identifier-A}] ORIGINAL [TOPIC{type-B,name-B,identifier-B}]",
            "EXIT [TOPIC{type-A,updated-name-A,identifier-A}] RETIRED [TOPIC{type-B,updated-name-B,identifier-B}]",
            "EXIT [] RETIRED [TOPIC{type-A,updated-name-A,identifier-A}]"
        )
    }

    @Test
    fun `Single extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticTopicProvider("A")) {
            listOf(extractValue, extractValue2)
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(1)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticCaptor.events).containsExactly(
            "ENTER [TOPIC{type-A,name-A,identifier-A}]",
            "EXIT [] RETIRED [TOPIC{type-A,name-A,identifier-A}]"
        )
    }

    @Test
    fun `Nested extractList reports proper diagnostic events`() {
        val ctx = DpmMappingContext.createRootContext(diagnostic)

        val extractRetValue = ctx.extractList(FixedDiagnosticTopicProvider("A")) {
            ctx.extractList(FixedDiagnosticTopicProvider("B")) {
                listOf(extractValue, extractValue2)
            }
        }

        assertThat(extractRetValue).size().isEqualTo(2)

        verify(extractRetValue[0], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[0])

        verify(extractRetValue[1], times(2)).validate(any())
        verifyNoMoreInteractions(extractRetValue[1])

        assertThat(diagnosticCaptor.events).containsExactly(
            "ENTER [TOPIC{type-A,name-A,identifier-A}]",
            "ENTER [TOPIC{type-B,name-B,identifier-B}, TOPIC{type-A,name-A,identifier-A}]",
            "EXIT [TOPIC{type-A,name-A,identifier-A}] RETIRED [TOPIC{type-B,name-B,identifier-B}]",
            "EXIT [] RETIRED [TOPIC{type-A,name-A,identifier-A}]"
        )
    }
}
