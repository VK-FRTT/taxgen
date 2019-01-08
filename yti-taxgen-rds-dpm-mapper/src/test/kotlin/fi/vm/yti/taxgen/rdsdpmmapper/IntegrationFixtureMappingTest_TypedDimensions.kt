package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@DisplayName("Mapping Integration Fixture to DPM model - TypedDimensions")
internal class IntegrationFixtureMappingTest_TypedDimensions
    : RdsToDpmMapper_UnitTestBase() {

    @Test
    fun `2 Typed Dimensions`() {
        val dpmDictionary = performMappingFromIntegrationFixture()

        dpmDictionary.typedDimensions.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.dimensionCode).isEqualTo("TDB-D1")

                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/taxgen-test-fixtures/typ-dims-2018-1/code/TDB-D1")
                    assertThat(it.type).isEqualTo("TypedDimension")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "TDB dimension 1")
                    )

                    assertThat(it.concept.description.translations).isEmpty()

                    assertThat(it.referencedDomainCode).isEqualTo("TDB")
                }

                1 -> {
                    assertThat(it.dimensionCode).isEqualTo("TDB-D2")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "TDB dimension 2")
                    )

                    assertThat(it.referencedDomainCode).isEqualTo("TDB")
                }

                else -> {
                    fail { "Unexpected item" }
                }
            }
        }
    }
}