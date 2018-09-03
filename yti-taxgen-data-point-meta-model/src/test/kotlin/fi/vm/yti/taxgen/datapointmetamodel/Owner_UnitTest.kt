package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
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
        "languages,             required",
        "defaultLanguage,       required"
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
        "name,                  maxLength,      100",
        "namespace,             minLength,      2",
        "namespace,             maxLength,      100",
        "prefix,                minLength,      2",
        "prefix,                maxLength,      50",
        "location,              minLength,      2",
        "location,              maxLength,      100",
        "copyright,             minLength,      2",
        "copyright,             maxLength,      1000",
        "languages,             minColLength,   1",
        "languages,             maxColLength,   10"
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
                if (property.name == "languages") {
                    Language.languages().take(length).toSet()
                } else {
                    null
                }
            }
        )
    }

    @Nested
    inner class LanguagesProp {

        @Test
        fun `languages should ignore duplicates without errors`() {
            attributeOverrides(
                "languages" to setOf(
                    language("fi"),
                    language("sv"),
                    language("fi")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
            assertThat(
                instance!!.languages.map { it.iso6391Code }.toList()
            ).containsExactlyInAnyOrderElementsOf(listOf("fi", "sv"))
        }
    }

    @Nested
    inner class CompanionObject {

        @Test
        fun `companion should provide diagnostic context info about owner`() {
            assertThat(Owner.Companion.contextType()).isEqualTo("Owner")
            assertThat(Owner.Companion.contextName()).isEqualTo("")
            assertThat(Owner.Companion.contextRef()).isEqualTo("")
        }
    }
}
