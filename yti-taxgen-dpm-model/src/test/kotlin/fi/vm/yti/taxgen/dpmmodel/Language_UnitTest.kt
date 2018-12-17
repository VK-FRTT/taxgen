package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.FailException
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.DPM_LANGUAGE_CONFIG
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.nio.file.Path

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
            assertThat(validationErrors)
                .containsExactly("Language.label: has too short translations for languages [en, fi]")
        }

        @Test
        fun `label should accept 5 characters long translation`() {
            attributeOverrides(
                "label" to TranslatedText(mapOf(language("en") to "12345"))
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class LanguageConfiguration {

        @Test
        fun `default language configuration should be available`() {
            Language.languages()
        }

        @Test
        fun `loading language configuration without default language should fail`() {
            val languageConfigPath: Path = TestFixture.pathOf(DPM_LANGUAGE_CONFIG, "default_language_en_missing.json")

            val thrown = catchThrowable { Language.Companion.loadLanguages(languageConfigPath) }

            assertThat(thrown)
                .isInstanceOf(FailException::class.java)
                .hasMessage("Language configuration missing mandatory default language 'en'")
        }

        @Test
        fun `loading language configuration with unsupported translation language should fail`() {
            val languageConfigPath: Path =
                TestFixture.pathOf(DPM_LANGUAGE_CONFIG, "label_translation_language_unsupported.json")
            val thrown = catchThrowable { Language.Companion.loadLanguages(languageConfigPath) }

            assertThat(thrown)
                .isInstanceOf(FailException::class.java)
                .hasMessage("Language configuration missing language 'sv' used for label 'engelska'")
        }

        @Test
        fun `loading language configuration with broken JSON syntax should fail`() {
            val languageConfigPath: Path =
                TestFixture.pathOf(DPM_LANGUAGE_CONFIG, "dpm_language_config_broken_json.json")
            val thrown = catchThrowable { Language.Companion.loadLanguages(languageConfigPath) }

            assertThat(thrown)
                .isInstanceOf(FailException::class.java)
                .hasMessageStartingWith("Language configuration loading failed: ")
        }

        @Test
        fun `loading language configuration without translations should fail`() {
            val languageConfigPath: Path = TestFixture.pathOf(DPM_LANGUAGE_CONFIG, "language_no_translations.json")

            val thrown = catchThrowable { Language.Companion.loadLanguages(languageConfigPath) }

            assertThat(thrown)
                .isInstanceOf(FailException::class.java)
                .hasMessage("Language configuration not valid. [Language #1 (fi): [Language.label: has too few translations (minimum 1)]]")
        }
    }
}