package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
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
        "uri,                       required",
        "concept,                   required",
        "metricCode,                required",
        "dataType,                  required",
        "flowType,                  optional",
        "balanceType,               optional",
        "referencedDomainCode,      optional",
        "referencedHierarchyCode,   optional"
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
        "metricCode,             minLength,      1",
        "metricCode,             maxLength,      50"
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
    inner class DataTypeProp {

        @DisplayName("dataType validation")
        @ParameterizedTest(name = "`{0}` should be {1} dataType with code tag {2}")
        @CsvSource(
            "Enumeration/Code,  valid,      e",
            "Boolean,           valid,      b",
            "Date,              valid,      d",
            "Integer,           valid,      i",
            "Monetary,          valid,      m",
            "Percent,           valid,      p",
            "String,            valid,      s",
            "Decimal,           valid,      d",
            "Lei,               valid,      l",
            "Isin,              valid,      i",
            "'',                invalid,     ",
            "null,              invalid,     ",
            "foo,               invalid,     "
        )
        fun `dataType should error if invalid`(
            dataType: String,
            expectedValidity: String,
            expectedCodeTag: String?
        ) {
            attributeOverrides(
                "dataType" to dataType
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> {
                    assertThat(validationErrors).isEmpty()
                    assertThat(Metric.codeTagFromDataType(dataType)).isEqualTo(expectedCodeTag)
                }

                "invalid" -> {
                    assertThat(validationErrors).containsExactly("Metric.dataType: unsupported data type '$dataType'")
                    assertThat(Metric.codeTagFromDataType(dataType)).isEqualTo("?")
                }

                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }

    @Nested
    inner class FlowTypeProp {

        @DisplayName("flowType validation")
        @ParameterizedTest(name = "`{0}` should be {1} flowType")
        @CsvSource(
            "Flow,          valid,      d",
            "Stock,         valid,      i",
            ",              valid,     ''", //null flowType value
            "'',            invalid,     ",
            "null,          invalid,     ",
            "foo,           invalid,     "
        )
        fun `flowType should error if invalid`(
            flowType: String?,
            expectedValidity: String,
            expectedCodeTag: String?
        ) {
            attributeOverrides(
                "flowType" to flowType
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> {
                    assertThat(validationErrors).isEmpty()
                    assertThat(Metric.codeTagFromFlowType(flowType)).isEqualTo(expectedCodeTag)
                }

                "invalid" -> {
                    assertThat(validationErrors).containsExactly("Metric.flowType: unsupported flow type '$flowType'")
                    assertThat(Metric.codeTagFromFlowType(flowType)).isEqualTo("?")
                }
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }

    @Nested
    inner class BalanceTypeProp {

        @DisplayName("balanceType validation")
        @ParameterizedTest(name = "`{0}` should be {1} balanceType")
        @CsvSource(
            "Credit,        valid",
            "Debit,         valid",
            ",              valid", //null
            "'',            invalid",
            "null,          invalid",
            "foo,           invalid"
        )
        fun `balanceType should error if invalid`(
            balanceType: String?,
            expectedValidity: String
        ) {
            attributeOverrides(
                "balanceType" to balanceType
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Metric.balanceType: unsupported balance type '$balanceType'")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }
    }

    @Nested
    inner class ReferencedDomainCodeProp {

        @Disabled
        @DisplayName("code validation")
        @ParameterizedTest(name = "code `{0}` should be {1} referencedDomainCode")
        @CsvSource(
            "1,         valid",
            "'',        invalid",
            "' ',       invalid"
        )
        fun `referencedDomainCode should error if invalid`(
            code: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "referencedDomainCode" to code
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Metric.referencedDomainCode: empty or blank id")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }

        @Test
        fun `referencedDomainCode should allow null value`() {
            attributeOverrides(
                "referencedDomainCode" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class ReferencedHierarchyCodeProp {

        @Disabled
        @DisplayName("code validation")
        @ParameterizedTest(name = "code `{0}` should be {1} referencedHierarchyCode")
        @CsvSource(
            "1,         valid",
            "'',        invalid",
            "' ',       invalid"
        )
        fun `referencedHierarchyCode should error if invalid`(
            code: String,
            expectedValidity: String
        ) {
            attributeOverrides(
                "referencedHierarchyCode" to code
            )

            instantiateAndValidate()

            when (expectedValidity) {
                "valid" -> assertThat(validationErrors).isEmpty()
                "invalid" -> assertThat(validationErrors).containsExactly("Metric.referencedHierarchyCode: empty or blank id")
                else -> thisShouldNeverHappen("Unsupported expectedValidity: $expectedValidity")
            }
        }

        @Test
        fun `referencedHierarchyCode should allow null value`() {
            attributeOverrides(
                "referencedHierarchyCode" to null
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }
}
