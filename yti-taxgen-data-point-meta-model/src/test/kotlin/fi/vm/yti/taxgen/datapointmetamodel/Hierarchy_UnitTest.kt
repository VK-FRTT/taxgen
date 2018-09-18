package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
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
        fun `rootNodes should error if memberCode {@ node # member) exists multiple times`() {
            val rootNodes = listOf(

                hierarchyNode(
                    member("m1", true)
                ),

                hierarchyNode(
                    member("m2", false),

                    hierarchyNode(
                        member("m3", false),

                        hierarchyNode(
                            member("m4", true)
                        )
                    )
                ),

                hierarchyNode(
                    member("m4", false)
                )
            )

            attributeOverrides(
                "rootNodes" to rootNodes
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: contains duplicate members [m4]")
        }

        @Test
        fun `rootNodes should error if single node instance exists multiple times`() {
            val node4 = hierarchyNode(
                member("m4", false)
            )

            val rootNodes = listOf(

                hierarchyNode(
                    member("m1", true)
                ),

                hierarchyNode(
                    member("m2", false),

                    hierarchyNode(
                        member("m3", true),

                        node4
                    )
                ),

                node4
            )

            attributeOverrides(
                "rootNodes" to rootNodes
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: contains duplicate members [m4]")
        }

        @Test
        fun `rootNodes should error if single member instance exists multiple times`() {
            val member4 = member("m4", false)

            val rootNodes = listOf(

                hierarchyNode(
                    member("m1", true)
                ),

                hierarchyNode(
                    member("m2", false),

                    hierarchyNode(
                        member("m3", true),

                        hierarchyNode(
                            member4
                        )
                    )
                ),

                hierarchyNode(
                    member4
                )
            )

            attributeOverrides(
                "rootNodes" to rootNodes
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("Hierarchy.rootNodes: contains duplicate members [m4]")
        }
    }
}
