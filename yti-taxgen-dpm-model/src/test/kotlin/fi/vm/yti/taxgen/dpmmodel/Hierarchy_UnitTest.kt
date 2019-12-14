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
    @ParameterizedTest(name = "{0} should be {1} property")
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
        fun `concept should produce validation error when it is not valid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[Hierarchy] [hie_uri] [Concept.Label] [Too few translations (minimum 1)]"
            )
        }
    }

    @Nested
    inner class RootNodesProp {

        @Test
        fun `rootNodes should produce validation error when it contains HierarchyNodes with duplicate URIs at root level`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", "member_m_1_uri"),
                    hierarchyNode("hn_2", "member_m_2_uri"),
                    hierarchyNode("hn_2", "member_m_3_uri"),
                    hierarchyNode("hn_4", "member_m_4_uri")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[Hierarchy] [hie_uri] [HierarchyNode.Uri] [Duplicate value] [hierarchy_node_hn_2_uri]"
            )
        }

        @Test
        fun `rootNodes should produce validation error when it contains HierarchyNodes with duplicate URIs in nested hierarchy`() {
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
            assertThat(validationErrors).containsExactly(
                "[Hierarchy] [hie_uri] [HierarchyNode.Uri] [Duplicate value] [hierarchy_node_hn_4_uri]"
            )
        }

        @Test
        fun `rootNodes should produce validation error when it contains HierarchyNodes with duplicate referencedElementCodes at root level`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", "member_m_1_code"),
                    hierarchyNode("hn_2", "member_m_2_code"),
                    hierarchyNode("hn_3", "member_m_2_code"),
                    hierarchyNode("hn_4", "member_m_4_code")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[Hierarchy] [hie_uri] [HierarchyNode.ReferencedElementCode] [Duplicate value] [member_m_2_code]"
            )
        }

        @Test
        fun `rootNodes should produce validation error when it contains HierarchyNodes with duplicate referencedElementCodes in nested hierarchy`() {
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
            assertThat(validationErrors).containsExactly(
                "[Hierarchy] [hie_uri] [HierarchyNode.ReferencedElementCode] [Duplicate value] [member_m_4_code]"
            )
        }
    }
}
