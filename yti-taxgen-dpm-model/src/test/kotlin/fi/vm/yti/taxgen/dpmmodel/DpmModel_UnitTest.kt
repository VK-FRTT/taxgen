package fi.vm.yti.taxgen.dpmmodel

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
    @ParameterizedTest(name = "{0} should be {1}")
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
        fun `dictionaries should have unique owner prefixes`() {

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
    }
}
