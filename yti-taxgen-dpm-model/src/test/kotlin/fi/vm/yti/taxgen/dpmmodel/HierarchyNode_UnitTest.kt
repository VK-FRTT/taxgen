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

internal class HierarchyNode_UnitTest :
    DpmModel_UnitTestBase<HierarchyNode>(HierarchyNode::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "id,                    required",
        "concept,               required",
        "abstract,              required",
        "comparisonOperator,    optional",
        "unaryOperator,         optional",
        "memberRef,             required",
        "childNodes,            optional"
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
        "id,                    minLength,      1",
        "id,                    maxLength,      128"
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
    inner class ComparisonOperatorProp {

        @DisplayName("comparisonOperator validation")
        @ParameterizedTest(name = "`{0}` should be {1} comparisonOperator")
        @CsvSource(
            "=,         valid",
            "<=,        valid",
            ">=,        valid",
            ",          valid", //NULL
            "==,        invalid",
            "<<,        invalid",
            "foo,       invalid"
        )
        fun `comparisonOperator should error if invalid`(
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
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }

    @Nested
    inner class UnaryOperatorProp {

        @DisplayName("unaryOperator validation")
        @ParameterizedTest(name = "`{0}` should be {1} unaryOperator")
        @CsvSource(
            "+,         valid",
            "-,         valid",
            ",          valid", //NULL
            "==,        invalid",
            "<<,        invalid",
            "foo,       invalid"
        )
        fun `unaryOperator should error if invalid`(
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
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }

    @Nested
    inner class MemberRefProp {

        @DisplayName("id validation")
        @ParameterizedTest(name = "id `{0}` should be {1} member ref")
        @CsvSource(
            "1,         valid",
            "'',        invalid",
            "' ',       invalid"
        )
        fun `memberRef should error if 'id' is invalid`(
            id: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "memberRef" to dpmElementRef<Member>(
                    id = id,
                    uri = "uri_value",
                    diagnosticLabel = "label_value"
                )
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("HierarchyNode.memberRef: empty or blank id")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }
}
