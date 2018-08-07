package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class ExplicitDomain_UnitTest :
    DpmModel_UnitTestBase<ExplicitDomain>(ExplicitDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "concept,               required",
        "domainCode,            required",
        "members,               required"
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
        "domainCode,            minLength,      2",
        "domainCode,            maxLength,      50",
        "members,               minColLength,   1",
        "members,               maxColLength,   10000"
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
                if (property.name == "members") {
                    List(length) { index -> member("$index", (index == 0)) }
                } else {
                    null
                }
            }
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
            Assertions.assertThat(validationErrors)
                .containsExactly("Concept.label: has too few translations (minimum 1)")
        }
    }

    @Nested
    inner class MembersProp {

        @Test
        fun `members should have unique memberCodes`() {
            attributeOverrides(
                "members" to listOf(
                    member("someCode", false),
                    member("duplicateCode", false),
                    member("duplicateCode", true),
                    member("anotherCode", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: contains duplicate elements [duplicateCode]")
        }

        @Test
        fun `members should error with 0 default member`() {
            attributeOverrides(
                "members" to listOf(
                    member("someCode", false),
                    member("fixedCode", false),
                    member("anotherCode", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 0 default members (should have 1)")
        }

        @Test
        fun `members should error with 2 default members`() {
            attributeOverrides(
                "members" to listOf(
                    member("someCode", false),
                    member("fixedCode", true),
                    member("anotherCode", true)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 2 default members (should have 1)")
        }

        @Test
        fun `members should accept 1 default member`() {
            attributeOverrides(
                "members" to listOf(
                    member("someCode", false),
                    member("fixedCode", true),
                    member("anotherCode", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors).isEmpty()
        }
    }
}
