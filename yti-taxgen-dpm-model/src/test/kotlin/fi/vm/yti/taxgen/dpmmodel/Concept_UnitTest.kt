package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import java.time.Instant
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class Concept_UnitTest :
    DpmModel_UnitTestBase<Concept>(Concept::class) {

    fun conceptValidationAdapter(
        instance: Any,
        validationResultBuilder: ValidationResultBuilder
    ) {
        (instance as Concept).validateConcept(
            validationResultBuilder = validationResultBuilder,
            minLabelLangCount = 1
        )
    }

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
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
        fun `createdAt should produce validation error when timestamp is not valid`() {
            attributeOverrides(
                "createdAt" to Instant.EPOCH
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [CreatedAt] [Illegal timestamp value] [1970-01-01T00:00:00Z]"
            )
        }
    }

    @Nested
    inner class ModifiedAtProp {

        @Test
        fun `modifiedAt should produce validation error when timestamp is not valid`() {
            attributeOverrides(
                "modifiedAt" to Instant.EPOCH
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactlyInAnyOrder(
                "[] [] [ModifiedAt] [Illegal timestamp value] [1970-01-01T00:00:00Z]",
                "[] [] [ModifiedAt] [Is earlier than CreatedAt] [1970-01-01T00:00:00Z]"
            )
        }

        @Test
        fun `modifiedAt should produce validation error when it precedes createdAt`() {
            attributeOverrides(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.400Z"),
                "modifiedAt" to Instant.parse("2018-03-19T10:20:30.400Z")
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [ModifiedAt] [Is earlier than CreatedAt] [2018-03-19T10:20:30.400Z]"
            )
        }

        @Test
        fun `modifiedAt should not produce validation error when it equals with createdAt`() {
            attributeOverrides(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.40Z"),
                "modifiedAt" to Instant.parse("2018-03-20T10:20:30.40Z")
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class ApplicableUntilProp {

        @Test
        fun `applicableUntil should produce validation error when it precedes applicableFrom`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to LocalDate.of(2018, 1, 19)
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [ApplicableUntil] [Is earlier than ApplicableFrom] [2018-01-19]"
            )
        }

        @Test
        fun `applicableUntil should not produce validation error when it equals with applicableFrom`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to LocalDate.of(2018, 1, 20)
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableFrom should not produce validation error when applicableUntil is undefined (null)`() {
            attributeOverrides(
                "applicableFrom" to LocalDate.of(2018, 1, 20),
                "applicableUntil" to null
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableUntil should not produce validation error when applicableFrom is undefined (null)`() {
            attributeOverrides(
                "applicableFrom" to null,
                "applicableUntil" to LocalDate.of(2018, 1, 20)
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `applicableUntil and applicableFrom should not produce validation error when they both are undefined (null)`() {
            attributeOverrides(
                "applicableFrom" to null,
                "applicableUntil" to null
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class LabelProp {

        @Test
        fun `label should produce validation error when it has 0 translations`() {
            attributeOverrides(
                "label" to TranslatedText(emptyMap())
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [Label] [Too few translations (minimum 1)]"
            )
        }

        @Test
        fun `label should not produce validation error when it has 1 translation`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fi") to "Jotain"
                    )
                )
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `label should produce validation error when it has 1 character long translation`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fi") to "1",
                        language("en") to "1"
                    )
                )
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [Label] [Too short translation content for languages] [en, fi]"
            )
        }

        @Test
        fun `label should not produce validation error when it has 2 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(mapOf(language("en") to "12"))
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `label should produce validation error when it has translation for language not supported by the Owner`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fr") to "12345-fr",
                        language("es") to "12345-es"
                    )
                )
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [Label] [Surplus translation languages] [es, fr]"
            )
        }
    }

    @Nested
    inner class Description {
        @Test
        fun `description should produce validation error when it has 1 character long translation`() {
            attributeOverrides(
                "description" to TranslatedText(
                    mapOf(
                        language("fi") to "1",
                        language("en") to "1"
                    )
                )
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [Description] [Too short translation content for languages] [en, fi]"
            )
        }

        @Test
        fun `description should not produce validation error when it has 2 characters long translation`() {
            attributeOverrides(
                "description" to TranslatedText(mapOf(language("en") to "12"))
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `description should produce validation error when it has translation for language not supported by the Owner`() {
            attributeOverrides(
                "description" to TranslatedText(
                    mapOf(
                        language("fr") to "12345-fr",
                        language("es") to "12345-es"
                    )
                )
            )

            instantiateAndValidate(::conceptValidationAdapter)
            assertThat(validationErrors).containsExactly(
                "[] [] [Description] [Surplus translation languages] [es, fr]"
            )
        }
    }
}
