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

internal class ExplicitDomain_UnitTest :
    DpmModel_UnitTestBase<ExplicitDomain>(ExplicitDomain::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "id,                    required",
        "concept,               required",
        "domainCode,            required",
        "members,               required"
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
        "id,                    maxLength,      128",
        "domainCode,            minLength,      2",
        "domainCode,            maxLength,      50",
        "members,               minColLength,   1",
        "members,               maxColLength,   10000"
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
                    List(length) { index -> member("$index", (index == 0)) }
                } else {
                    null
                }
            }
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
    inner class MembersProp {

        @Test
        fun `members should have unique ids and memberCodes`() {
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
                .containsExactly(
                    "ExplicitDomain.members: duplicate id value 'm_2_id'",
                    "ExplicitDomain.members: duplicate memberCode value 'm_2_code'"
                )
        }

        @Test
        fun `members should error with 0 default member`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", false),
                    member("m_3", false)
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 0 default members (should have 1)")
        }

        @Test
        fun `members should error with 2 default members`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_1", false),
                    member("m_2", true),
                    member("m_3", true)
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 2 default members (should have 1)")
        }

        @Test
        fun `members should accept 1 default member`() {
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
        fun `hierarchies should have unique ids and hierarchyCodes`() {
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
                    "ExplicitDomain.hierarchies: duplicate hierarchyCode value 'h_2_code'",
                    "ExplicitDomain.hierarchies: duplicate id value 'h_2_id'"
                )
        }
    }

    @Test
    fun `hierarchies should refer only Members which are from the Domain itself`() {

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
                        refTo<Member>("m_1"),

                        hierarchyNode(
                            "hn_1.2",
                            refTo<Member>("m_2"),

                            hierarchyNode(
                                "hn_1.3",
                                refTo<Member>("m_3") //External
                            )
                        )
                    )
                ),

                hierarchy(
                    "h_2",
                    hierarchyNode(
                        "hn_2",
                        refTo<Member>("m_1"),

                        hierarchyNode(
                            "hn_2.1",
                            refTo<Member>("m_2"),

                            hierarchyNode(
                                "hn_2.2",
                                refTo<Member>("m_4"), //External

                                hierarchyNode(
                                    "hn_2.3",
                                    refTo<Member>("m_5") //External
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
                "ExplicitDomain.hierarchies: DPM Hierarchy contains to DPM Member, which is not part of the DPM Domain. (DPM Member) m_3_diagnostic_label (m_3_uri) in (DPM HierarchyNode) Text#fi (hn_1.3_uri) at (DPM Hierarchy) Text#fi (h_1_uri)",
                "ExplicitDomain.hierarchies: DPM Hierarchy contains to DPM Member, which is not part of the DPM Domain. (DPM Member) m_4_diagnostic_label (m_4_uri) in (DPM HierarchyNode) Text#fi (hn_2.2_uri) at (DPM Hierarchy) Text#fi (h_2_uri)",
                "ExplicitDomain.hierarchies: DPM Hierarchy contains to DPM Member, which is not part of the DPM Domain. (DPM Member) m_5_diagnostic_label (m_5_uri) in (DPM HierarchyNode) Text#fi (hn_2.3_uri) at (DPM Hierarchy) Text#fi (h_2_uri)"
            )
    }
}
