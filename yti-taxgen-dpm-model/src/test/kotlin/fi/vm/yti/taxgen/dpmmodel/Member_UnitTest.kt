package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class Member_UnitTest :
    DpmModel_UnitTestBase<Member>(Member::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "uri,               required",
        "concept,           required",
        "memberCode,        required",
        "defaultMember,     required"
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
        "uri,                   minLength,      1",
        "uri,                   maxLength,      500",
        "memberCode,            minLength,      1",
        "memberCode,            maxLength,      50"
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
    inner class ConceptProp {

        @Test
        fun `concept should error if invalid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Concept.label: has too few translations (minimum 1)")
        }
    }

    @Nested
    inner class MemberCodeProp {

        @DisplayName("memberCode content validation")
        @ParameterizedTest(name = "`{0}` should be {1} memberCode")
        @CsvSource(
            "a,         valid",
            "A,         valid",
            "z,         valid",
            "Z,         valid",
            "_,         valid",
            ":,         valid",
            "aa,        valid",
            "aA,        valid",
            "az,        valid",
            "aZ,        valid",
            "a0,        valid",
            "a1,        valid",
            "a9,        valid",
            "a.,        valid",
            "a-,        valid",
            "a_,        valid",
            "a:,        valid",
            "' ',       invalid",
            "'a ',      invalid",
            "1,         invalid",
            "1a,        invalid",
            "å,         invalid",
            "Å,         invalid",
            "aå,        invalid",
            "aÅ,        invalid"
        )
        fun testPropertyLengthValidation(
            codeValue: String?,
            expectedValidity: String
        ) {
            attributeOverrides(
                "memberCode" to codeValue
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Member.memberCode: is illegal DPM Code")
                else -> thisShouldNeverHappen("Unsupported expected validity $expectedValidity")
            }
        }
    }
}
