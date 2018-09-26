package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.datafactory.Factory
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
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
                    List(length) { index -> member("$index", "$index", (index == 0)) }
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
            Assertions.assertThat(validationErrors)
                .containsExactly("Concept.label: has too few translations (minimum 1)")
        }
    }

    @Nested
    inner class MembersProp {

        @Test
        fun `members should have unique ids`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_id_1", "m_code_1", false),
                    member("m_id_2", "m_code_2", false),
                    member("m_id_2", "m_code_3", true),
                    member("m_id_4", "m_code_4", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: id has duplicate values [m_id_2]")
        }

        @Test
        fun `members should have unique memberCodes`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_id_1", "m_code_1", false),
                    member("m_id_2", "m_code_2", false),
                    member("m_id_3", "m_code_2", true),
                    member("m_id_4", "m_code_4", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: memberCode has duplicate values [m_code_2]")
        }

        @Test
        fun `members should error with 0 default member`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_id_1", "m_code_1", false),
                    member("m_id_2", "m_code_2", false),
                    member("m_id_3", "m_code_3", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 0 default members (should have 1)")
        }

        @Test
        fun `members should error with 2 default members`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_id_1", "m_code_1", false),
                    member("m_id_2", "m_code_2", true),
                    member("m_id_3", "m_code_3", true)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.members: has 2 default members (should have 1)")
        }

        @Test
        fun `members should accept 1 default member`() {
            attributeOverrides(
                "members" to listOf(
                    member("m_id_1", "m_code_1", false),
                    member("m_id_2", "m_code_2", true),
                    member("m_id_3", "m_code_3", false)
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors).isEmpty()
        }
    }

    @Nested
    inner class HierarchiesProp {

        @Test
        fun `hierarchies should have unique ids`() {
            attributeOverrides(
                "hierarchies" to listOf(
                    hierarchy("h_id_1", "h_code_1"),
                    hierarchy("h_id_2", "h_code_2"),
                    hierarchy("h_id_2", "h_code_3"),
                    hierarchy("h_id_4", "h_code_4")
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.hierarchies: id has duplicate values [h_id_2]")
        }

        @Test
        fun `hierarchies should have unique hierarchyCodes`() {
            attributeOverrides(
                "hierarchies" to listOf(
                    hierarchy("h_id_1", "h_code_1"),
                    hierarchy("h_id_2", "h_code_2"),
                    hierarchy("h_id_3", "h_code_2"),
                    hierarchy("h_id_4", "h_code_4")
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly("ExplicitDomain.hierarchies: hierarchyCode has duplicate values [h_code_2]")
        }
    }

    @Test
    fun `hierarchies should refer only Members which are from the Domain itself`() {
        attributeOverrides(
            "members" to listOf(
                member("m_id_1", "m_code_1", false),
                member("m_id_2", "m_code_2", true)
            ),

            "hierarchies" to listOf(
                hierarchy(
                    "h_id_1",
                    "h_code_1",
                    hierarchyNode(
                        "hn_id_1_1",
                        dpmElementRef<Member>("m_id_1"),

                        hierarchyNode(
                            "hn_id_1_2",
                            dpmElementRef<Member>("m_id_2"),

                            hierarchyNode(
                                "hn_id_1_3",
                                dpmElementRef<Member>("m_id_3")
                            )
                        )
                    )
                ),

                hierarchy(
                    "h_id_2",
                    "h_code_2",
                    hierarchyNode(
                        "hn_id_2_1",
                        dpmElementRef<Member>("m_id_1"),

                        hierarchyNode(
                            "hn_id_2_2",
                            dpmElementRef<Member>("m_id_2"),

                            hierarchyNode(
                                "hn_id_2_3",
                                dpmElementRef<Member>("m_id_4"),

                                hierarchyNode(
                                    "hn_id_2_4",
                                    dpmElementRef<Member>("m_id_5")
                                )
                            )
                        )
                    )
                )
            )
        )

        instantiateAndValidate()
        Assertions.assertThat(validationErrors)
            .containsExactly(
                "ExplicitDomain.hierarchies: Hierarchy h_code_1 has Members which do not belong to Domain [m_id_3])",
                "ExplicitDomain.hierarchies: Hierarchy h_code_2 has Members which do not belong to Domain [m_id_4, m_id_5])"
            )
    }
}
