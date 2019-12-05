package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DpmModel_UnitTest :
    DpmModel_UnitTestBase<DpmModel>(DpmModel::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "dictionaries,          required"
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
    inner class DictionariesProp {

        @Test
        fun `dictionaries should produce validation error when Owner prefixes are not unique`() {

            attributeOverrides(
                "dictionaries" to listOf(
                    emptyDictionaryWithOwner("o_1"),
                    emptyDictionaryWithOwner("o_2"),
                    emptyDictionaryWithOwner("o_2"),
                    emptyDictionaryWithOwner("o_4")
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly(
                    "DpmModel.dictionaries: duplicate owner.prefix value 'prefix_o_2'"
                )
        }

        @Test
        fun `dictionaries should produce validation error when Domain URIs and codes are not globally unique`() {

            fun dictionaryWithDomains(
                ownerBaseId: String,
                expDomBaseIds: List<String>,
                typDomBaseIds: List<String>
            ): DpmDictionary {
                return emptyDictionaryWithOwner(ownerBaseId).copy(
                    explicitDomains = expDomBaseIds.map {
                        ExplicitDomain(
                            uri = "dom_${it}_uri",
                            domainCode = "dom_${it}_code",
                            concept = Factory.instantiate(),
                            members = emptyList(),
                            hierarchies = emptyList()
                        )
                    },
                    typedDomains = typDomBaseIds.map {
                        TypedDomain(
                            uri = "dom_${it}_uri",
                            domainCode = "dom_${it}_code",
                            concept = Factory.instantiate(),
                            dataType = "String"
                        )
                    }
                )
            }

            attributeOverrides(
                "dictionaries" to listOf(
                    dictionaryWithDomains("o_1", listOf("e_1"), listOf("t_1")),
                    dictionaryWithDomains("o_2", listOf("e_2"), listOf("t_2", "d_1")),
                    dictionaryWithDomains("o_3", listOf("e_3", "d_1"), listOf("t_3")),
                    dictionaryWithDomains("o_4", listOf("e_4"), listOf("t_4"))
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly(
                    "DpmModel.dictionaries.domains: duplicate Domain.code value 'dom_d_1_code'",
                    "DpmModel.dictionaries.domains: duplicate Domain.uri value 'dom_d_1_uri'"
                )
        }

        @Test
        fun `dictionaries should produce validation error when MetricDomain's Metric URIs and codes are not globally unique`() {

            fun dictionaryWithMetricDomainAndMetricBaseIds(
                ownerBaseId: String,
                metricBaseIds: List<String>
            ): DpmDictionary {
                return emptyDictionaryWithOwner(ownerBaseId).copy(
                    metricDomain = metricDomain().copy(
                        metrics = metricBaseIds.map { metric(it) }
                    )
                )
            }

            attributeOverrides(
                "dictionaries" to listOf(
                    dictionaryWithMetricDomainAndMetricBaseIds("o_1", listOf("m_1")),
                    dictionaryWithMetricDomainAndMetricBaseIds("o_2", listOf("m_2")),
                    dictionaryWithMetricDomainAndMetricBaseIds("o_3", listOf("m_2")),
                    dictionaryWithMetricDomainAndMetricBaseIds("o_4", listOf("m_4", "m_5"))
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly(
                    "DpmModel.dictionaries.metricDomain: duplicate Metric.metricCode value 'met_m_2_code'",
                    "DpmModel.dictionaries.metricDomain: duplicate Metric.uri value 'met_m_2_uri'"
                )
        }

        @Test
        fun `dictionaries should produce validation error when MetricDomain's Hierarchy URIs and codes are not globally unique`() {

            fun dictionaryWithMetricDomainAndHierarchyBaseIds(
                ownerBaseId: String,
                hierarchyBaseIds: List<String>
            ): DpmDictionary {
                return emptyDictionaryWithOwner(ownerBaseId).copy(
                    metricDomain = metricDomain().copy(
                        metrics = emptyList(),
                        hierarchies = hierarchyBaseIds.map { hierarchy(it) }
                    )
                )
            }

            attributeOverrides(
                "dictionaries" to listOf(
                    dictionaryWithMetricDomainAndHierarchyBaseIds("o_1", listOf("h_1")),
                    dictionaryWithMetricDomainAndHierarchyBaseIds("o_2", listOf("h_2")),
                    dictionaryWithMetricDomainAndHierarchyBaseIds("o_3", listOf("h_2")),
                    dictionaryWithMetricDomainAndHierarchyBaseIds("o_4", listOf("h_4", "h_5"))
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly(
                    "DpmModel.dictionaries.metricDomain: duplicate Hierarchy.hierarchyCode value 'hierarchy_h_2_code'",
                    "DpmModel.dictionaries.metricDomain: duplicate Hierarchy.uri value 'hierarchy_h_2_uri'"
                )
        }
    }
}
