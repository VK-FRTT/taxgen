package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class Metric_UnitTest :
    DpmModel_UnitTestBase<Metric>(Metric::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "id,                    required",
        "concept,               required",
        "memberCodeNumber,      required",
        "dataType,              required",
        "flowType,              required",
        "balanceType,           required",
        "domainRef,             optional",
        "hierarchyRef,          optional"
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
    inner class DomainRefProp {

        @DisplayName("id validation")
        @ParameterizedTest(name = "id `{0}` should be {1} member ref")
        @CsvSource(
            "1,         valid",
            "'',        invalid",
            "' ',       invalid"
        )
        fun `domainRef should error if 'id' is invalid`(
            id: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "domainRef" to dpmElementRef<ExplicitDomain>(
                    id = id,
                    uri = "uri_value",
                    diagnosticLabel = "label_value"
                )
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Metric.domainRef: empty or blank id")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }

        @Test
        fun `domainRef should allow null value`() {
            attributeOverrides(
                "domainRef" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }


    @Nested
    inner class HierarchyRefProp {

        @DisplayName("id validation")
        @ParameterizedTest(name = "id `{0}` should be {1} member ref")
        @CsvSource(
            "1,         valid",
            "'',        invalid",
            "' ',       invalid"
        )
        fun `hierarchyRef should error if 'id' is invalid`(
            id: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "hierarchyRef" to dpmElementRef<ExplicitDomain>(
                    id = id,
                    uri = "uri_value",
                    diagnosticLabel = "label_value"
                )
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Metric.hierarchyRef: empty or blank id")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }


        @Test
        fun `hierarchyRef should allow null value`() {
            attributeOverrides(
                "hierarchyRef" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }
}
