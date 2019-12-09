package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class RdsToDpmMapper_IntegrationFixtureContent_TypedDimensions_ModuleTest :
    RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `2 Typed Dimensions`() {
        val dpmDictionary = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(dpmDictionary.typedDimensions.size).isEqualTo(2)

        dpmDictionary.typedDimensions.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.dimensionCode).isEqualTo("TDB-D1")

                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-dims-2018-1/code/TDB-D1")
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
