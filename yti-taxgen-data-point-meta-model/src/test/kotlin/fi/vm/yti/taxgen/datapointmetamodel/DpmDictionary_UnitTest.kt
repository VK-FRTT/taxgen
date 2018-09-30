package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.DpmModel_UnitTestBase
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.unitestbase.propertyOptionalityTemplate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class DpmDictionary_UnitTest :
    DpmModel_UnitTestBase<DpmDictionary>(DpmDictionary::class) {

    @DisplayName("Property optionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "owner,                 required",
        "explicitDomains,       required"
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
        "explicitDomains,       minColLength,       1",
        "explicitDomains,       maxColLength,   10000"
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
                if (property.name == "explicitDomains") {
                    List(length) { index -> explicitDomain("ex-domain-$index") }
                } else {
                    null
                }
            }
        )
    }

    @Nested
    inner class PropertyValidation {

        @Nested
        inner class ExplicitDomainsProp {

            @Test
            fun `explicitDomains should have unique domainCodes`() {
                attributeOverrides(
                    "explicitDomains" to listOf(
                        explicitDomain("domain-1"),
                        explicitDomain("duplicateDomain"),
                        explicitDomain("duplicateDomain"),
                        explicitDomain("domain-42")
                    )
                )

                instantiateAndValidate()
                Assertions.assertThat(validationErrors)
                    .containsExactly("DpmDictionary.explicitDomains: duplicate domainCode value 'duplicateDomain'")
            }
        }
    }
}
