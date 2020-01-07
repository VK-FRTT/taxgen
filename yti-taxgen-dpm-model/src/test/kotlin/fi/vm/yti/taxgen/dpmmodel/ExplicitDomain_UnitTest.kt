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

internal class ExplicitDomain_UnitTest :
    DpmModel_UnitTestBase<ExplicitDomain>(ExplicitDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "uri,                   required",
        "concept,               required",
        "domainCode,            required",
        "members,               required",
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
        "members,               maxColLength,   10000",
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
                if (property.name == "members") {
                    mapOf("members" to List(length) { index -> member("$index", (index == 0)) })
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
        fun `concept should produce validation error when it is not valid`() {
            attributeOverrides(
                "concept" to Factory.instantiateWithOverrides<Concept>(
                    "label" to TranslatedText(emptyMap())
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "[ExplicitDomain] [exp_dom_uri] [Concept.Label] [Too few translations (minimum 1)]"
                )
        }
    }

    @Nested
    inner class MembersProp {

        @Test
        fun `members should produce validation error when it contains Members with duplicate URIs or memberCodes`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", false),
                    member("m_2", true),
                    member("m_4", false)
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactlyInAnyOrder(
                    "[ExplicitDomain] [exp_dom_uri] [Member] [member_m_2_uri] [Uri] [Duplicate value] [member_m_2_uri]",
                    "[ExplicitDomain] [exp_dom_uri] [Member] [member_m_2_uri] [MemberCode] [Duplicate value] [member_m_2_code]"
                )
        }

        @Test
        fun `members should produce validation error when it contains more than 1 default Member`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", true),
                    member("m_3", true)
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "[ExplicitDomain] [exp_dom_uri] [Member.DefaultMember] [Too many default members (maximum 1)] [2]"
                )
        }

        @Test
        fun `members should not produce validation error when it contains 1 default Member`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", true),
                    member("m_3", false)
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class HierarchiesProp {

        @Test
        fun `hierarchies should produce validation error when it contains Hierarchies with duplicate URIs or hierarchyCodes`() {
            attributeOverrides(
                "hierarchies" to listOf(
                    hierarchy("h_1"),
                    hierarchy("h_2"),
                    hierarchy("h_2"),
                    hierarchy("h_4")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "[ExplicitDomain] [exp_dom_uri] [Hierarchy] [hierarchy_h_2_uri] [Uri] [Duplicate value] [hierarchy_h_2_uri]",
                    "[ExplicitDomain] [exp_dom_uri] [Hierarchy] [hierarchy_h_2_uri] [HierarchyCode] [Duplicate value] [hierarchy_h_2_code]"
                )
        }

        @Test
        fun `hierarchies should produce validation error when HierarchyNodes refer to Members which are not part of the ExplicitDomain`() {

            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", true)
                ),

                "hierarchies" to listOf(
                    hierarchy(
                        "h_1",
                        hierarchyNode(
                            "hn_1",
                            "member_m_1_code"
                        ),

                        hierarchyNode(
                            "hn_1.2",
                            "member_m_2_code",

                            hierarchyNode(
                                "hn_1.3",
                                "member_m_3_code" // External
                            )
                        )
                    ),

                    hierarchy(
                        "h_2",
                        hierarchyNode(
                            "hn_2",
                            "member_m_1_code",

                            hierarchyNode(
                                "hn_2.1",
                                "member_m_2_code",

                                hierarchyNode(
                                    "hn_2.2",
                                    "member_m_4_code", // External

                                    hierarchyNode(
                                        "hn_2.3",
                                        "member_m_5_code" // External
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
                    "[ExplicitDomain] [exp_dom_uri] [Hierarchy] [hierarchy_h_1_uri] [HierarchyNode] [hierarchy_node_hn_1.3_uri] [ReferencedElementCode] [Unknown target] [member_m_3_code]",
                    "[ExplicitDomain] [exp_dom_uri] [Hierarchy] [hierarchy_h_2_uri] [HierarchyNode] [hierarchy_node_hn_2.2_uri] [ReferencedElementCode] [Unknown target] [member_m_4_code]",
                    "[ExplicitDomain] [exp_dom_uri] [Hierarchy] [hierarchy_h_2_uri] [HierarchyNode] [hierarchy_node_hn_2.3_uri] [ReferencedElementCode] [Unknown target] [member_m_5_code]"
                )
        }
    }
}
