package fi.vm.yti.taxgen.datapointmetamodel

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

internal class Hierarchy_UnitTest :
    DpmModel_UnitTestBase<Hierarchy>(Hierarchy::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "id,                required",
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
    inner class RootNodesProp {

        @Test
        fun `rootNodes should have unique ids {within flat root}`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", refTo<Member>("m_1")),
                    hierarchyNode("hn_2", refTo<Member>("m_2")),
                    hierarchyNode("hn_2", refTo<Member>("m_3")),
                    hierarchyNode("hn_4", refTo<Member>("m_4"))
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: duplicate id value 'hn_2_id'")
        }

        @Test
        fun `rootNodes should have unique ids {within hierarchy}`() {
            attributeOverrides(
                "rootNodes" to listOf(

                    hierarchyNode(
                        "hn_1",
                        refTo<Member>("m_1")
                    ),

                    hierarchyNode(
                        "hn_2",
                        refTo<Member>("m_2"),

                        hierarchyNode(
                            "hn_3",
                            refTo<Member>("m_3"),

                            hierarchyNode(
                                "hn_4",
                                refTo<Member>("m_4")
                            )
                        )
                    ),

                    hierarchyNode(
                        "hn_4",
                        refTo<Member>("m_5")
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: duplicate id value 'hn_4_id'")
        }

        @Test
        fun `rootNodes should have unique memberRefs {within flat root}`() {
            attributeOverrides(
                "rootNodes" to listOf(
                    hierarchyNode("hn_1", refTo<Member>("m_1")),
                    hierarchyNode("hn_2", refTo<Member>("m_2")),
                    hierarchyNode("hn_3", refTo<Member>("m_2")),
                    hierarchyNode("hn_4", refTo<Member>("m_4"))
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "Hierarchy.rootNodes: DPM Hierarchy contains multiple times same DPM Member. (DPM Member) m_2_diagnostic_label (m_2_uri) in (DPM HierarchyNode) Text#fi (hn_2_uri)",
                    "Hierarchy.rootNodes: DPM Hierarchy contains multiple times same DPM Member. (DPM Member) m_2_diagnostic_label (m_2_uri) in (DPM HierarchyNode) Text#fi (hn_3_uri)"
                )
        }

        @Test
        fun `rootNodes should have unique memberRefs {within hierarchy}`() {
            attributeOverrides(
                "rootNodes" to listOf(

                    hierarchyNode(
                        "hn_1",
                        refTo<Member>("m_1")
                    ),

                    hierarchyNode(
                        "hn_2",
                        refTo<Member>("m_2"),

                        hierarchyNode(
                            "hn_3",
                            refTo<Member>("m_3"),

                            hierarchyNode(
                                "hn_4",
                                refTo<Member>("m_4")
                            )
                        )
                    ),

                    hierarchyNode(
                        "hn_5",
                        refTo<Member>("m_4")
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly(
                    "Hierarchy.rootNodes: DPM Hierarchy contains multiple times same DPM Member. (DPM Member) m_4_diagnostic_label (m_4_uri) in (DPM HierarchyNode) Text#fi (hn_4_uri)",
                    "Hierarchy.rootNodes: DPM Hierarchy contains multiple times same DPM Member. (DPM Member) m_4_diagnostic_label (m_4_uri) in (DPM HierarchyNode) Text#fi (hn_5_uri)"
                )
        }
    }
}
