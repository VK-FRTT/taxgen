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

internal class Hierarchy_UnitTest :
    DpmModel_UnitTestBase<Hierarchy>(Hierarchy::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "uri,               required",
        "concept,           required",
        "hierarchyCode,     required",
        "rootNodes,         required"
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
        "uri,                    maxLength,      500"
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
    inner class RootNodesProp {

        @Test
        fun `rootNodes should have unique URIs {within flat root}`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", "member_m_1_uri"),
                    hierarchyNode("hn_2", "member_m_2_uri"),
                    hierarchyNode("hn_2", "member_m_3_uri"),
                    hierarchyNode("hn_4", "member_m_4_uri")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: duplicate uri value 'hierarchy_node_hn_2_uri'")
        }

        @Test
        fun `rootNodes should have unique URIs {within hierarchy}`() {
            attributeOverrides(
                "rootNodes" to listOf(

                    hierarchyNode(
                        "hn_1",
                        "member_m_1_uri"
                    ),

                    hierarchyNode(
                        "hn_2",
                        "member_m_2_uri",

                        hierarchyNode(
                            "hn_3",
                            "member_m_3_uri",

                            hierarchyNode(
                                "hn_4",
                                "member_m_4_uri"
                            )
                        )
                    ),

                    hierarchyNode(
                        "hn_4",
                        "member_m_5_uri"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: duplicate uri value 'hierarchy_node_hn_4_uri'")
        }

        @Test
        fun `rootNodes should have unique referencedElementCodes {within flat root}`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", "member_m_1_code"),
                    hierarchyNode("hn_2", "member_m_2_code"),
                    hierarchyNode("hn_3", "member_m_2_code"),
                    hierarchyNode("hn_4", "member_m_4_code")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "Hierarchy.rootNodes: duplicate referencedElementCode value 'member_m_2_code'"
                )
        }

        @Test
        fun `rootNodes should have unique referencedElementCodes {within hierarchy}`() {
            attributeOverrides(
                "rootNodes" to listOf(

                    hierarchyNode(
                        "hn_1",
                        "member_m_1_code"
                    ),

                    hierarchyNode(
                        "hn_2",
                        "member_m_2_code",

                        hierarchyNode(
                            "hn_3",
                            "member_m_3_code",

                            hierarchyNode(
                                "hn_4",
                                "member_m_4_code"
                            )
                        )
                    ),

                    hierarchyNode(
                        "hn_5",
                        "member_m_4_code"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "Hierarchy.rootNodes: duplicate referencedElementCode value 'member_m_4_code'"
                )
        }
    }
}
