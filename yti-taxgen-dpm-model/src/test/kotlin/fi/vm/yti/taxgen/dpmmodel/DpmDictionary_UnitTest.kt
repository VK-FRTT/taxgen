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

internal class DpmDictionary_UnitTest :
    DpmModel_UnitTestBase<DpmDictionary>(DpmDictionary::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1} property")
    @CsvSource(
        "owner,                 required",
        "metricDomain,          optional",
        "explicitDomains,       required",
        "typedDomains,          required",
        "explicitDimensions,    required",
        "typedDimensions,       required"
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
        "explicitDomains,       maxColLength,   10000",
        "typedDomains,          maxColLength,   10000",
        "explicitDimensions,    maxColLength,   10000",
        "typedDimensions,       maxColLength,   10000"
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

                when (property.name) {
                    "metricDomains" ->
                        mapOf(
                            "metricDomains" to List(length) { metricDomain() },
                            "explicitDomains" to listOf(Factory.instantiate<ExplicitDomain>())
                        )

                    "explicitDomains" ->
                        mapOf("explicitDomains" to List(length) { index -> explicitDomain("$index") })

                    "typedDomains" ->
                        mapOf("typedDomains" to List(length) { index -> typedDomain("$index") })

                    "explicitDimensions" ->
                        mapOf(
                            "explicitDimensions" to List(length) { index -> explicitDimension("$index", "exp_dom") },
                            "explicitDomains" to listOf(Factory.instantiate<ExplicitDomain>())
                        )

                    "typedDimensions" ->
                        mapOf(
                            "typedDimensions" to List(length) { index -> typedDimension("$index", "typ_dom") },
                            "typedDomains" to listOf(Factory.instantiate<TypedDomain>())
                        )
                    else ->
                        emptyMap()
                }
            }
        )
    }

    @Nested
    inner class DomainProps {

        @Test
        fun `should produce validation error when ExplicitDomains and TypedDomains URIs and domainCodes are not unique within DPM Dictionary`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    ),
                    ExplicitDomain(
                        uri = "exp_dom_2_uri",
                        domainCode = "exp_dom_2_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    )
                ),

                "typedDomains" to listOf(
                    TypedDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        dataType = "String"
                    ),

                    TypedDomain(
                        uri = "typ_dom_3_uri",
                        domainCode = "typ_dom_3_code",
                        concept = Factory.instantiate(),
                        dataType = "String"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [Domain.DomainCode] [Duplicate value] [dom_1_code]",
                "[DPM Dictionary] [ns_prefix] [Domain.Uri] [Duplicate value] [dom_1_uri]"
            )
        }
    }

    @Nested
    inner class DimensionProps {

        @Test
        fun `should produce validation error when ExplicitDimensions and TypedDimensions URIs and domainCodes are not unique within DPM Dictionary`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    explicitDomain("e1")
                ),

                "typedDomains" to listOf(
                    typedDomain("t1")
                ),

                "explicitDimensions" to listOf(
                    ExplicitDimension(
                        uri = "dim_1_uri",
                        dimensionCode = "dim_1_code",
                        concept = Factory.instantiate(),
                        referencedDomainCode = "exp_dom_e1_code"
                    ),

                    ExplicitDimension(
                        uri = "exp_dim_2_uri",
                        dimensionCode = "exp_dim_2_code",
                        concept = Factory.instantiate(),
                        referencedDomainCode = "exp_dom_e1_code"
                    )
                ),

                "typedDimensions" to listOf(
                    TypedDimension(
                        uri = "dim_1_uri",
                        dimensionCode = "dim_1_code",
                        concept = Factory.instantiate(),
                        referencedDomainCode = "typ_dom_t1_code"
                    ),

                    TypedDimension(
                        uri = "typ_dim_3_uri",
                        dimensionCode = "typ_dim_3_code",
                        concept = Factory.instantiate(),
                        referencedDomainCode = "typ_dom_t1_code"
                    )
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [Dimension.DimensionCode] [Duplicate value] [dim_1_code]",
                "[DPM Dictionary] [ns_prefix] [Dimension.Uri] [Duplicate value] [dim_1_uri]"
            )
        }

        @Test
        fun `should produce validation error when ExplicitDimension refer unknown ExplicitDomain`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    )
                ),

                "typedDomains" to listOf(
                    TypedDomain(
                        uri = "dom_2_uri",
                        domainCode = "dom_2_code",
                        concept = Factory.instantiate(),
                        dataType = "String"
                    )
                ),

                "explicitDimensions" to listOf(
                    explicitDimension("e1", "dom_1_code"),
                    explicitDimension("e2", "dom_2_code")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [ExplicitDimension] [exp_dim_e2_uri] [ReferencedDomainCode] [Refers to unknown target] [dom_2_code]"
            )
        }

        @Test
        fun `should produce validation error when TypedDimension refer unknown TypedDomain`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    )
                ),

                "typedDomains" to listOf(
                    TypedDomain(
                        uri = "dom_2_uri",
                        domainCode = "dom_2_code",
                        concept = Factory.instantiate(),
                        dataType = "String"
                    )
                ),

                "typedDimensions" to listOf(
                    typedDimension("t1", "dom_1_code"),
                    typedDimension("t2", "dom_2_code")
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [TypedDimension] [typ_dim_t1_uri] [ReferencedDomainCode] [Refers to unknown target] [dom_1_code]"
            )
        }
    }

    @Nested
    inner class MetricsProp {

        @Test
        fun `should produce validation error when Metric referencedDomainCode refers unknown ExplicitDomain`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    )
                ),

                "metricDomain" to MetricDomain(
                    uri = "MET",
                    domainCode = "MET",
                    concept = Factory.instantiate(),
                    metrics = listOf(
                        Metric(
                            uri = "met_1_uri",
                            metricCode = "met_1_code",
                            concept = Factory.instantiate(),
                            dataType = "Enumeration/Code",
                            flowType = "Instant",
                            balanceType = "Credit",
                            referencedDomainCode = "unknown_dom",
                            referencedHierarchyCode = null
                        )
                    ),
                    hierarchies = listOf()
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [Metric] [met_1_uri] [ReferencedDomainCode] [Refers to unknown target] [unknown_dom]"
            )
        }

        @Test
        fun `should produce validation error when Metric referencedHierarchyCode refers unknown Hierarchy`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = emptyList()
                    )
                ),

                "metricDomain" to MetricDomain(
                    uri = "MET",
                    domainCode = "MET",
                    concept = Factory.instantiate(),
                    metrics = listOf(
                        Metric(
                            uri = "met_1_uri",
                            metricCode = "met_1_code",
                            concept = Factory.instantiate(),
                            dataType = "Enumeration/Code",
                            flowType = "Instant",
                            balanceType = "Credit",
                            referencedDomainCode = "dom_1_code",
                            referencedHierarchyCode = "unknown_hier"
                        )
                    ),
                    hierarchies = listOf()
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).containsExactly(
                "[DPM Dictionary] [ns_prefix] [Metric] [met_1_uri] [ReferencedHierarchyCode] [Refers to unknown target] [unknown_hier]"
            )
        }

        @Test
        fun `should not produce validation error when Metric referencedDomainCode and referencedHierarchyCode refers known elements`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    ExplicitDomain(
                        uri = "dom_1_uri",
                        domainCode = "dom_1_code",
                        concept = Factory.instantiate(),
                        members = emptyList(),
                        hierarchies = listOf(
                            hierarchy(
                                baseId = "h_1"
                            )
                        )
                    )
                ),

                "metricDomain" to MetricDomain(
                    uri = "MET",
                    domainCode = "MET",
                    concept = Factory.instantiate(),
                    metrics = listOf(
                        Metric(
                            uri = "met_1_uri",
                            metricCode = "met_1_code",
                            concept = Factory.instantiate(),
                            dataType = "Enumeration/Code",
                            flowType = "Instant",
                            balanceType = "Credit",
                            referencedDomainCode = "dom_1_code",
                            referencedHierarchyCode = "hierarchy_h_1_code"
                        )
                    ),
                    hierarchies = listOf()
                )
            )

            instantiateAndValidate()
            assertThat(validationErrors).isEmpty()
        }
    }
}
