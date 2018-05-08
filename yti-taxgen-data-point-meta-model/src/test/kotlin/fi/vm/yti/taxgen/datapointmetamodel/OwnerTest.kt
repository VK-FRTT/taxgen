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
        "namespace,             required",
        "namespacePrefix,       required",
        "officialLocation,      required",
        "copyrightText,         required",
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
        "namespace,             minLength,      10",
        "namespace,             maxLength,      100",
        "namespacePrefix,       minLength,      2",
        "namespacePrefix,       maxLength,      10",
        "officialLocation,      minLength,      10",
        "officialLocation,      maxLength,      100",
        "supportedLanguages,    minLength,      1",
        "supportedLanguages,    maxLength,      50"
    )
    fun testPropertyLengthValidation(testedProperty: String, validationType: String, expectedLimit: Int) {
        propertyLengthValidationTemplate<Owner>(testedProperty, validationType, expectedLimit)
    }
}
