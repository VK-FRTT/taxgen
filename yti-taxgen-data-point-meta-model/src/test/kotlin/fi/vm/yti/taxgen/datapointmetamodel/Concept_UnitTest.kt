package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.Instant
import java.time.LocalDate

internal class Concept_UnitTest :
    DpmModel_UnitTestBase<Concept>(Concept::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "label,             required",
        "description,       required",
        "createdAt,         required",
        "modifiedAt,        required",
        "applicableFrom,    optional",
        "applicableUntil,   optional",
        "owner,             required"
    )
    fun testPropertyOptionality(
        propertyName: String,
        expectedOptionality: String
    ) {
        propertyOptionalityTemplate(
            propertyName = propertyName,
            expectedOptionality = expectedOptionality
        )
    }

    @Nested
    inner class CreatedAtProp {

        @Test
        fun `createdAt should be valid timestamp`() {
            attributeOverrides(
                "createdAt" to Instant.EPOCH
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.createdAt: is illegal timestamp value")
        }
    }

    @Nested
    inner class ModifiedAtProp {

        @Test
        fun `modifiedAt should be valid timestamp`() {
            attributeOverrides(
                "modifiedAt" to Instant.EPOCH
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactlyInAnyOrder(
                "Concept.modifiedAt: is illegal timestamp value",
                "Concept.modifiedAt: is earlier than createdAt"
            )
        }

        @Test
        fun `modifiedAt must come after createdAt`() {
            attributeOverrides(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.40Z"),
                "modifiedAt" to Instant.parse("2018-03-19T10:20:30.40Z")
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.modifiedAt: is earlier than createdAt")
        }

        @Test
        fun `modifiedAt may be equal with createdAt`() {
            attributeOverrides(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.40Z"),
                "modifiedAt" to Instant.parse("2018-03-20T10:20:30.40Z")
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class ApplicableUntilProp {

        @Test
        fun `applicableUntil must come after applicableFrom`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to LocalDate.of(2018, 1, 19)
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.applicableUntil: is earlier than applicableFrom")
        }

        @Test
        fun `applicableUntil may be equal with applicableFrom`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to LocalDate.of(2018, 1, 20)
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableFrom may be defined without applicableUntil`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableUntil may be defined without applicableFrom`() {
            attributeOverrides(
                "applicableFrom" to null,
                "applicableUntil" to LocalDate.of(2018, 1, 20)
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableUntil and applicableFrom both may be undefined`() {
            attributeOverrides(
                "applicableFrom" to null,
                "applicableUntil" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class LabelProp {

        @Test
        fun `label should error with 0 translations`() {
            attributeOverrides(
                "label" to TranslatedText(emptyMap())
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.label: has too few translations (minimum 1)")
        }

        @Test
        fun `label should succeed with 1 translation`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fi") to "Jotain"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `label should error with 1 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fi") to "1",
                        language("en") to "1"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.label: has too short translations for languages [en, fi]")
        }

        @Test
        fun `label should accept 2 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(mapOf(language("en") to "12"))
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `label should error with not Owner defined translation languages`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fr") to "12345-fr",
                        language("es") to "12345-es"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.label: contains translations with surplus languages [es, fr]")
        }
    }

    @Nested
    inner class Description {
        @Test
        fun `description should error with 1 characters long translation`() {
            attributeOverrides(
                "description" to TranslatedText(
                    mapOf(
                        language("fi") to "1",
                        language("en") to "1"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.description: has too short translations for languages [en, fi]")
        }

        @Test
        fun `description should accept 2 characters long translation`() {
            attributeOverrides(
                "description" to TranslatedText(mapOf(language("en") to "12"))
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `description should error with not Owner defined translation languages`() {
            attributeOverrides(
                "description" to TranslatedText(
                    mapOf(
                        language("fr") to "12345-fr",
                        language("es") to "12345-es"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Concept.description: contains translations with surplus languages [es, fr]")
        }
    }
}
