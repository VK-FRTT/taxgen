package fi.vm.yti.taxgen.dpmmodel

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

internal class MetricDomain_UnitTest :
    DpmModel_UnitTestBase<MetricDomain>(MetricDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "uri,                   required",
        "concept,               required",
        "domainCode,            required",
        "metrics,               required",
        "hierarchies,           required"
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
        "domainCode,            minLength,      2",
        "domainCode,            maxLength,      50",
        "metrics,               maxColLength,   10000",
        "hierarchies,           maxColLength,   10000"
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
                if (property.name == "metrics") {
                    mapOf("metrics" to List(length) { index -> metric("$index") })
                } else if (property.name == "hierarchies") {
                    mapOf("hierarchies" to List(length) { index -> hierarchy("$index") })
                } else {
                    emptyMap()
                }
            }
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
    inner class HierarchiesProp {

        @Test
        fun `hierarchies should produce validation error when HierarchyNodes refer to Metrics which are not part of the MetricDomain`() {

            attributeOverrides(
                "metrics" to listOf(
                    metric("m_1"),
                    metric("m_2")
                ),

                "hierarchies" to listOf(
                    hierarchy(
                        "h_1",
                        hierarchyNode(
                            "hn_1",
                            "met_m_1_code"
                        ),

                        hierarchyNode(
                            "hn_1.2",
                            "met_m_2_code",

                            hierarchyNode(
                                "hn_1.3",
                                "met_m_3_code" // External
                            )
                        )
                    ),

                    hierarchy(
                        "h_2",
                        hierarchyNode(
                            "hn_2",
                            "met_m_1_code",

                            hierarchyNode(
                                "hn_2.1",
                                "met_m_2_code",

                                hierarchyNode(
                                    "hn_2.2",
                                    "met_m_4_code", // External

                                    hierarchyNode(
                                        "hn_2.3",
                                        "met_m_5_code" // External
                                    )
                                )
                            )
                        )
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_1.3_uri refers to DPM Metric which is not present in DPM MetricDomain.",
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_2.2_uri refers to DPM Metric which is not present in DPM MetricDomain.",
                    "MetricDomain.hierarchies: DPM HierarchyNode hierarchy_node_hn_2.3_uri refers to DPM Metric which is not present in DPM MetricDomain."
                )
        }
    }
}
