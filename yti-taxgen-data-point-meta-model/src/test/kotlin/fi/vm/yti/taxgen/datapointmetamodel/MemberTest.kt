package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.Factory
import fi.vm.yti.taxgen.datapointmetamodel.testtemplates.propertyLengthValidationTemplate
import fi.vm.yti.taxgen.datapointmetamodel.testtemplates.propertyOptionalityTemplate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MemberTest {

    @BeforeEach
    fun init() {
        Factory.registerDefinitions(dataPointMetaModelTestData())
    }

    @DisplayName("PropertyOptionality")
    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource(
        "concept,           required",
        "memberCode,        required",
        "defaultMember,     required"
    )
    fun testPropertyOptionality(testedProperty: String, expectedOptionality: String) {
        propertyOptionalityTemplate<Member>(
            testedProperty,
            expectedOptionality
        )
    }

    @DisplayName("PropertyLengthValidation")
    @ParameterizedTest(name = "{0} {1} should be {2}")
    @CsvSource(
        "memberCode,            minLength,      2",
        "memberCode,            maxLength,      50"
    )
    fun testPropertyLengthValidation(testedProperty: String, validationType: String, expectedLimit: Int) {
        propertyLengthValidationTemplate<Member>(testedProperty, validationType, expectedLimit)
    }
}
