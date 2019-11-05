package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.Factory
import fi.vm.yti.taxgen.dpmmodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.dpmmodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
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
    inner class ExplicitDomainsProp {

        @Test
        fun `explicitDomains should produce validation error when ExplicitDomain's URIs or domainCodes are not unique within DPM Dictionary`() {

            attributeOverrides(
                "explicitDomains" to listOf(
                    explicitDomain("d_1"),
                    explicitDomain("d_2"),
                    explicitDomain("d_2"),
                    explicitDomain("d_4")
                )
            )

            instantiateAndValidate()
            Assertions.assertThat(validationErrors)
                .containsExactly(
                    "DpmDictionary.domains: duplicate domainCode value 'exp_dom_d_2_code'",
                    "DpmDictionary.domains: duplicate uri value 'exp_dom_d_2_uri'"
                )
        }
    }
}
