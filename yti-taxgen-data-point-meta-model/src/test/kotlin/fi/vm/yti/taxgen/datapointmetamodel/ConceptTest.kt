package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.Factory
import fi.vm.yti.taxgen.datapointmetamodel.testtemplates.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate


internal class ConceptTest {

    @BeforeEach
    fun init() {
        Factory.registerDefinitions(dataPointMetaModelTestData())
    }

    @DisplayName("PropertyOptionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "owner,             required",
        "label,             required",
        "description,       optional",
        "createdAt,         required",
        "modifiedAt,        optional",
        "applicableFrom,    optional",
        "applicableUntil,   optional"
    )
    fun testPropertyOptionality(testedProperty: String, expectedOptionality: String) {
        propertyOptionalityTemplate<Concept>(
            testedProperty,
            expectedOptionality
        )
    }


    @Nested
    inner class PropertyValidation {

        @Test
        fun `createdAt should precede modifiedAt`() {
            val attributes = Factory.attributesFor<Concept>(
                overrides = mapOf(
                    "createdAt" to LocalDate.of(2018, 1, 20),
                    "modifiedAt" to LocalDate.of(2018, 1, 19)
                )
            )

            val exception = catchThrowable { Factory.instantiate<Concept>(attributes) }
            assertThat(exception.cause)
                .isInstanceOf(java.lang.IllegalArgumentException::class.java)
                .hasMessageContaining("createdAt")
                .hasMessageContaining("modifiedAt")
        }

        @Test
        fun `createdAt may be equal with modifiedAt`() {
            val attributes = Factory.attributesFor<Concept>(
                overrides = mapOf(
                    "createdAt" to LocalDate.of(2018, 1, 20),
                    "modifiedAt" to LocalDate.of(2018, 1, 20)
                )
            )

            val concept = Factory.instantiate<Concept>(attributes)
            assertThat(concept.createdAt).isEqualTo(concept.modifiedAt)
        }


        @Test
        fun `applicableFrom should precede applicableUntil`() {
            val attributes = Factory.attributesFor<Concept>(
                overrides = mapOf(
                    "applicableFrom" to LocalDate.of(2018, 1, 20),
                    "applicableUntil" to LocalDate.of(2018, 1, 19)
                )
            )

            val exception = catchThrowable { Factory.instantiate<Concept>(attributes) }
            assertThat(exception.cause)
                .isInstanceOf(java.lang.IllegalArgumentException::class.java)
                .hasMessageContaining("applicableFrom")
                .hasMessageContaining("applicableUntil")
        }


        @Test
        fun `applicableFrom may be equal with applicableUntil`() {
            val attributes = Factory.attributesFor<Concept>(
                overrides = mapOf(
                    "applicableFrom" to LocalDate.of(2018, 1, 20),
                    "applicableUntil" to LocalDate.of(2018, 1, 20)
                )
            )

            val concept = Factory.instantiate<Concept>(attributes)
            assertThat(concept.applicableFrom).isEqualTo(concept.applicableUntil)
        }


        @Test
        fun `label may not be empty`() {
            val attributes = Factory.attributesFor<Concept>(
                overrides = mapOf(
                    "label" to TranslatedText(translations = emptyMap())
                )
            )

            val exception = catchThrowable { Factory.instantiate<Concept>(attributes) }
            assertThat(exception.cause)
                .isInstanceOf(java.lang.IllegalArgumentException::class.java)
                .hasMessageContaining("label")
        }
    }
}