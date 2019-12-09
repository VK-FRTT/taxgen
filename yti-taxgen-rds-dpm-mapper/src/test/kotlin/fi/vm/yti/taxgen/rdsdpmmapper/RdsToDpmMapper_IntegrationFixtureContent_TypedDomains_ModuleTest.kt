package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class RdsToDpmMapper_IntegrationFixtureContent_TypedDomains_ModuleTest :
    RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `8 Typed Domains`() {
        val dpmDictionary = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(dpmDictionary.typedDomains.size).isEqualTo(8)

        dpmDictionary.typedDomains.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.domainCode).isEqualTo("DOMT")

                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/typ-doms-2018-1/code/DOMT")
                    assertThat(it.type).isEqualTo("TypedDomain")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (fi, label)")
                    )

                    assertThat(it.concept.description.translations).containsOnly(
                        entry(fi, "Typed domain (fi, description)")
                    )

                    assertThat(it.dataType).isEqualTo("Boolean")
                }

                1 -> {
                    assertThat(it.domainCode).isEqualTo("TDB")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Boolean)")
                    )

                    assertThat(it.dataType).isEqualTo("Boolean")
                }

                2 -> {
                    assertThat(it.domainCode).isEqualTo("TDD")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Date)")
                    )

                    assertThat(it.dataType).isEqualTo("Date")
                }

                3 -> {
                    assertThat(it.domainCode).isEqualTo("TDI")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Integer)")
                    )

                    assertThat(it.dataType).isEqualTo("Integer")
                }

                4 -> {
                    assertThat(it.domainCode).isEqualTo("TDM")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Monetary)")
                    )

                    assertThat(it.dataType).isEqualTo("Monetary")
                }

                5 -> {
                    assertThat(it.domainCode).isEqualTo("TDP")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Percentage)")
                    )

                    assertThat(it.dataType).isEqualTo("Percent")
                }

                6 -> {
                    assertThat(it.domainCode).isEqualTo("TDR")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (Decimal)")
                    )

                    assertThat(it.dataType).isEqualTo("Decimal")
                }

                7 -> {
                    assertThat(it.domainCode).isEqualTo("TDS")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Typed domain (String)")
                    )

                    assertThat(it.dataType).isEqualTo("String")
                }

                else -> {
                    fail { "Unexpected item" }
                }
            }
        }
    }
}
