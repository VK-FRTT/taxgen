package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.exception.throwIllegalDpmModelState
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class HierarchyNode_UnitTest :
    DpmModel_UnitTestBase<HierarchyNode>(HierarchyNode::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "uri,                   required",
        "concept,               required",
        "abstract,              required",
        "comparisonOperator,    optional",
        "unaryOperator,         optional",
        "referencedElementCode, required",
        "childNodes,            required"
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
        "uri,                    minLength,      1",
        "uri,                    maxLength,      500",
        "referencedElementCode,  minLength,      1",
        "referencedElementCode,  maxLength,      50"
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
        fun `concept should not produce validation error when label has 0 translations (differs from other DPM Elements)`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }

        @Test
        fun `concept should produce validation error when it is not valid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(listOf(Language.byIso6391CodeOrFail("en") to "").toMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Concept.label: has too short translations for languages [en]")
        }
    }

    @Nested
    inner class ComparisonOperatorProp {

        @ParameterizedTest(name = "`{0}` should be {1} comparisonOperator")
        @CsvSource(
            "=,         valid",
            "<=,        valid",
            ">=,        valid",
            "<,         valid",
            ">,         valid",
            ",          valid", // NULL
            "==,        invalid",
            "<<,        invalid",
            "foo,       invalid"
        )
        fun testComparisonOperatorValidation(
            comparisonOperator: String?,
            expectedValidity: String
        ) {
            attributeOverrides(
                "comparisonOperator" to comparisonOperator
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("HierarchyNode.comparisonOperator: unsupported arithmetical relationship (comparison operator) '$comparisonOperator'")
                else -> throwIllegalDpmModelState()
            }
        }
    }

    @Nested
    inner class UnaryOperatorProp {

        @ParameterizedTest(name = "`{0}` should be {1} unaryOperator")
        @CsvSource(
            "+,         valid",
            "-,         valid",
            ",          valid", // NULL
            "==,        invalid",
            "<<,        invalid",
            "foo,       invalid"
        )
        fun testUnaryOperatorValidation(
            unaryOperator: String?,
            expectedValidity: String
        ) {
            attributeOverrides(
                "unaryOperator" to unaryOperator
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("HierarchyNode.unaryOperator: unsupported arithmetical sign (unary operator) '$unaryOperator'")
                else -> throwIllegalDpmModelState()
            }
        }
    }
}
