package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

@DisplayName("Mapping Integration Fixture to DPM model - ExplicitDomainsAndHierarchies")
internal class IntegrationFixtureMappingTest_Metrics
    : RdsToDpmMapper_UnitTestBase() {

    @Test
    fun `13 Metrics`() {
        val dpmDictionary = performMappingFromIntegrationFixture()

        assertThat(dpmDictionary.metrics.size).isEqualTo(13)

        dpmDictionary.metrics.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("1")

                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/taxgen-test-fixtures/metrics-2018-1/code/1")
                    assertThat(it.type).isEqualTo("Metric")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (fi, label)")
                    )

                    assertThat(it.concept.description.translations).containsOnly(
                        entry(fi, "MET member (fi, description)")
                    )

                    assertThat(it.dataType).isEqualTo("Boolean")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                1 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("3")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Enumeration: EDA)")
                    )

                    assertThat(it.dataType).isEqualTo("Enumeration")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isEqualTo("EDA")
                    assertThat(it.referencedHierarchyCode).isEqualTo("EDA-H1")
                }

                2 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("4")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Boolean)")
                    )

                    assertThat(it.dataType).isEqualTo("Boolean")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                3 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("6")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Integer)")
                    )

                    assertThat(it.dataType).isEqualTo("Integer")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                4 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("7")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Monetary)")
                    )

                    assertThat(it.dataType).isEqualTo("Monetary")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                5 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("8")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Percentage)")
                    )

                    assertThat(it.dataType).isEqualTo("Percentage")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                6 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("9")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (String)")
                    )

                    assertThat(it.dataType).isEqualTo("String")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                7 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("10")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Decimal)")
                    )

                    assertThat(it.dataType).isEqualTo("Decimal")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                8 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("11")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Lei)")
                    )

                    assertThat(it.dataType).isEqualTo("Lei")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                9 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("12")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Isin)")
                    )

                    assertThat(it.dataType).isEqualTo("Isin")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                10 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("13")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (String, Instant, Credit)")
                    )

                    assertThat(it.dataType).isEqualTo("String")
                    assertThat(it.flowType).isEqualTo("Instant")
                    assertThat(it.balanceType).isEqualTo("Credit")
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                11 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("14")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (String, Duration, Debit)")
                    )

                    assertThat(it.dataType).isEqualTo("String")
                    assertThat(it.flowType).isEqualTo("Duration")
                    assertThat(it.balanceType).isEqualTo("Debit")
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                12 -> {
                    assertThat(it.memberCodeNumber).isEqualTo("16")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "MET member (Date)")
                    )

                    assertThat(it.dataType).isEqualTo("Date")
                    assertThat(it.flowType).isNull()
                    assertThat(it.balanceType).isNull()
                    assertThat(it.referencedDomainCode).isNull()
                    assertThat(it.referencedHierarchyCode).isNull()
                }

                else -> {
                    fail { "Unexpected item" }
                }
            }
        }
    }
}
