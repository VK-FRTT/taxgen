package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class Owner_UnitTest :
    DpmModel_UnitTestBase<Owner>(Owner::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "name,                  required",
        "namespace,             required",
        "prefix,                required",
        "location,              required",
        "copyright,             required",
        "languageCodes,         required",
        "defaultLanguageCode,   required"
    )
    fun testPropertyOptionality(
        propertyName: String,
        expectedOptionality: String
    ) {
        propertyOptionalityTemplate(
            propertyName,
            expectedOptionality
        )
    }

    @DisplayName("Property length validation")
    @ParameterizedTest(name = "{0} {1} should be {2}")
    @CsvSource(
        "name,                  minLength,      2",
        "name,                  maxLength,      500",
        "namespace,             minLength,      2",
        "namespace,             maxLength,      500",
        "prefix,                minLength,      2",
        "prefix,                maxLength,      500",
        "location,              minLength,      2",
        "location,              maxLength,      500",
        "copyright,             minLength,      2",
        "copyright,             maxLength,      500",
        "languageCodes,         minColLength,   1",
        "languageCodes,         maxColLength,   10"
    )
    fun testPropertyLengthValidation(
        propertyName: String,
        validationType: String,
        expectedLimit: Int
    ) {
        propertyLengthValidationTemplate(
            propertyName = propertyName,
            validationType = validationType,
            expectedLimit = expectedLimit,
            customValueBuilder = { property, length ->
                if (property.name == "languageCodes") {
                    Language.languages().take(length).map { it.iso6391Code }
                } else {
                    null
                }
            }
        )
    }

    @Nested
    inner class LanguageCodesProp {

        @Test
        fun `languageCodes should detect duplicates`() {
            attributeOverrides(
                "languageCodes" to listOf("fi", "sv", "fi")
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Owner.languages: duplicate language code value 'fi'")
        }

        @Test
        fun `languageCodes should detect unknown codes`() {
            attributeOverrides(
                "languageCodes" to listOf("fi", "zyx", "en")
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Owner.languages: unsupported language 'zyx'")
        }

        @Test
        fun `languageCodes should be mapped to languages`() {
            attributeOverrides(
                "languageCodes" to listOf("fi", "sv", "en")
            )

            instantiateAndValidate()
            assertThat(instance!!.languages.map { it.iso6391Code })
                .containsExactly("fi", "sv", "en")
        }
    }

    @Nested
    inner class DefaultLanguageCodeProp {

        @Test
        fun `defaultLanguageCode should detect unknown codes`() {
            attributeOverrides(
                "defaultLanguageCode" to "foobar"
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Owner.defaultLanguage: unsupported default language 'foobar'")
        }

        @Test
        fun `defaultLanguageCode should be mapped to language`() {
            attributeOverrides(
                "defaultLanguageCode" to "sv"
            )

            instantiateAndValidate()
            assertThat(instance!!.defaultLanguage.iso6391Code).isEqualTo("sv")
        }
    }
}
