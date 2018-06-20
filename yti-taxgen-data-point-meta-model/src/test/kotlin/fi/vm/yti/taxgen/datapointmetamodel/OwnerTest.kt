package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.Factory
import fi.vm.yti.taxgen.datapointmetamodel.testtemplates.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.testtemplates.propertyOptionalityTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class OwnerTest {

    @BeforeEach
    fun init() {
        Factory.registerDefinitions(dataPointMetaModelTestData())
    }

    @DisplayName("PropertyOptionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "name,                  required",
        "namespace,             required",
        "prefix,                required",
        "location,              required",
        "copyright,             required",
        "supportedLanguages,    required"
    )
    fun testPropertyOptionality(testedProperty: String, expectedOptionality: String) {
        propertyOptionalityTemplate<Owner>(
            testedProperty,
            expectedOptionality
        )
    }

    @DisplayName("PropertyLengthValidation")
    @ParameterizedTest(name = "{0} {1} should be {2}")
    @CsvSource(
        "name,                  minLength,      10",
        "name,                  maxLength,      100",
        "namespace,             minLength,      10",
        "namespace,             maxLength,      100",
        "prefix,                minLength,      2",
        "prefix,                maxLength,      10",
        "location,              minLength,      10",
        "location,              maxLength,      100",
        "supportedLanguages,    minLength,      1",
        "supportedLanguages,    maxLength,      50"
    )
    fun testPropertyLengthValidation(testedProperty: String, validationType: String, expectedLimit: Int) {
        propertyLengthValidationTemplate<Owner>(testedProperty, validationType, expectedLimit)
    }
}
