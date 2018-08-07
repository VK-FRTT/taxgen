package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class Language_UnitTest :
    DpmModel_UnitTestBase<Language>(Language::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "iso6391Code,       required",
        "label,             required"
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

    @DisplayName("Property length validation")
    @ParameterizedTest(name = "{0} {1} should be {2}")
    @CsvSource(
        "iso6391Code,           minLength,      2",
        "iso6391Code,           maxLength,      2"
    )
    fun testPropertyLengthValidation(
        propertyName: String,
        validationType: String,
        expectedLimit: Int
    ) {
        propertyLengthValidationTemplate(
            propertyName = propertyName,
            validationType = validationType,
            expectedLimit = expectedLimit
        )
    }

    @Nested
    inner class LabelProp {
        @Test
        fun `label should error with 0 translations`() {
            attributeOverrides(
                "label" to TranslatedText(emptyMap())
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Language.label: has too few translations (minimum 1)")
        }

        @Test
        fun `label should accept 1 translations`() {
            attributeOverrides(
                "label" to TranslatedText(emptyMap())
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly("Language.label: has too few translations (minimum 1)")
        }

        @Test
        fun `label should error with 4 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(
                    mapOf(
                        language("fi") to "1234",
                        language("en") to "1234"
                    )
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("Language.label: has too short translations for languages [en, fi]")
        }

        @Test
        fun `label should accept 5 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(mapOf(language("en") to "12345"))
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors).isEmpty()
        }
    }
}
