package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class RdsToDpmMapper_IntegrationFixtureContent_Metrics_ModuleTest
    : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `1 Metric Domain`() {
        val dpmDictionary = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(dpmDictionary.metricDomains.size).isEqualTo(1)

        dpmDictionary.metricDomains.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.domainCode).isEqualTo("MET")
                    assertThat(it.uri).isEqualTo("MET")
                    assertThat(it.type).isEqualTo("MetricDomain")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isNull()
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).isEmpty()
                    assertThat(it.concept.description.translations).isEmpty()
                }
            }
        }
    }

    @Nested
    inner class MetricDomain_MET {

        private val domainCode = "MET"

        @Test
        fun `13 Metrics`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().metricDomains.find { it.domainCode == domainCode }!!

            domain.metrics.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.metricCode).isEqualTo("b1")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/code/1")
                        assertThat(it.type).isEqualTo("Metric")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Metric member (fi, label)")
                        )

                        assertThat(it.concept.description.translations).containsOnly(
                            entry(fi, "Metric member (fi, description)")
                        )

                        assertThat(it.dataType).isEqualTo("Boolean")
                        assertThat(it.flowType).isNull()
                        assertThat(it.balanceType).isNull()
                        assertThat(it.referencedDomainCode).isNull()
                        assertThat(it.referencedHierarchyCode).isNull()
                    }

                    1 -> {
                        assertThat(it.metricCode).isEqualTo("e3")

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET member (Enumeration: EDA)")
                        )

                        assertThat(it.dataType).isEqualTo("Enumeration/Code")
                        assertThat(it.flowType).isNull()
                        assertThat(it.balanceType).isNull()
                        assertThat(it.referencedDomainCode).isEqualTo("EDA")
                        assertThat(it.referencedHierarchyCode).isEqualTo("EDA-H1")
                    }

                    2 -> {
                        assertThat(it.metricCode).isEqualTo("b4")

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
                        assertThat(it.metricCode).isEqualTo("i6")

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
                        assertThat(it.metricCode).isEqualTo("m7")

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
                        assertThat(it.metricCode).isEqualTo("p8")

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET member (Percentage)")
                        )

                        assertThat(it.dataType).isEqualTo("Percent")
                        assertThat(it.flowType).isNull()
                        assertThat(it.balanceType).isNull()
                        assertThat(it.referencedDomainCode).isNull()
                        assertThat(it.referencedHierarchyCode).isNull()
                    }

                    6 -> {
                        assertThat(it.metricCode).isEqualTo("s9")

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
                        assertThat(it.metricCode).isEqualTo("d10")

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
                        assertThat(it.metricCode).isEqualTo("l11")

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
                        assertThat(it.metricCode).isEqualTo("i12")

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
                        assertThat(it.metricCode).isEqualTo("si13")

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET member (String, Instant, Credit)")
                        )

                        assertThat(it.dataType).isEqualTo("String")
                        assertThat(it.flowType).isEqualTo("Stock")
                        assertThat(it.balanceType).isEqualTo("Credit")
                        assertThat(it.referencedDomainCode).isNull()
                        assertThat(it.referencedHierarchyCode).isNull()
                    }

                    11 -> {
                        assertThat(it.metricCode).isEqualTo("sd14")

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET member (String, Duration, Debit)")
                        )

                        assertThat(it.dataType).isEqualTo("String")
                        assertThat(it.flowType).isEqualTo("Flow")
                        assertThat(it.balanceType).isEqualTo("Debit")
                        assertThat(it.referencedDomainCode).isNull()
                        assertThat(it.referencedHierarchyCode).isNull()
                    }

                    12 -> {
                        assertThat(it.metricCode).isEqualTo("d16")

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

        @Test
        fun `3 Hierarchies`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().metricDomains.find { it.domainCode == domainCode }!!

            assertThat(domain.hierarchies.size).isEqualTo(3)

            domain.hierarchies.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.hierarchyCode).isEqualTo("MET1")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Metric hierarchy 1")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    1 -> {
                        assertThat(it.hierarchyCode).isEqualTo("MET10")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET10")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Metric hierarchy 10")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    2 -> {
                        assertThat(it.hierarchyCode).isEqualTo("METHIER")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/METHIER")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Metric hierarchy (fi, label)")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `10 HierarchyNode within 'MET1' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().metricDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "MET1" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(10)

            hierarchy.allNodes().forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/MET1/member/1")
                        assertThat(it.type).isEqualTo("HierarchyNode")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isNull()
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (String)")
                        )

                        assertThat(it.concept.description.translations).isEmpty()

                        assertThat(it.abstract).isFalse()
                        assertThat(it.comparisonOperator).isNull()
                        assertThat(it.unaryOperator).isNull()

                        assertThat(it.referencedElementCode).isEqualTo("s9")
                        assertThat(it.childNodes).isEmpty()
                    }

                    1 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Decimal)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("d10")
                        assertThat(it.childNodes).isEmpty()
                    }

                    2 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Lei)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("l11")
                        assertThat(it.childNodes).isEmpty()
                    }

                    3 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Isin)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("i12")
                        assertThat(it.childNodes).isEmpty()
                    }

                    4 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Enumeration: EDA)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("e3")
                        assertThat(it.childNodes).isEmpty()
                    }

                    5 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Date)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("d16")
                        assertThat(it.childNodes).isEmpty()
                    }

                    6 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Boolean)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("b4")
                        assertThat(it.childNodes).isEmpty()
                    }

                    7 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Integer)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("i6")
                        assertThat(it.childNodes).isEmpty()
                    }

                    8 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Monetary)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("m7")
                        assertThat(it.childNodes).isEmpty()
                    }

                    9 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Percentage)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("p8")
                        assertThat(it.childNodes).isEmpty()
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `12 HierarchyNode within 'MET10' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().metricDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "MET10" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(12)

            hierarchy.allNodes().forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Boolean)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("b4")
                        assertThat(it.childNodes).isEmpty()
                    }

                    1 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Enumeration: EDA)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("e3")
                        assertThat(it.childNodes).isEmpty()
                    }

                    2 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Date)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("d16")
                        assertThat(it.childNodes[0].referencedElementCode).isEqualTo("s9")
                        assertThat(it.childNodes[1].referencedElementCode).isEqualTo("si13")
                        assertThat(it.childNodes[2].referencedElementCode).isEqualTo("m7")
                        assertThat(it.childNodes.size).isEqualTo(3)
                    }

                    3 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (String)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("s9")
                        assertThat(it.childNodes).isEmpty()
                    }

                    4 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (String, Instant, Credit)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("si13")
                        assertThat(it.childNodes[0].referencedElementCode).isEqualTo("sd14")
                        assertThat(it.childNodes.size).isEqualTo(1)
                    }

                    5 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (String, Duration, Debit)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("sd14")
                        assertThat(it.childNodes).isEmpty()
                    }

                    6 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Monetary)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("m7")
                        assertThat(it.childNodes[0].referencedElementCode).isEqualTo("l11")
                        assertThat(it.childNodes[1].referencedElementCode).isEqualTo("p8")
                        assertThat(it.childNodes[2].referencedElementCode).isEqualTo("i12")
                        assertThat(it.childNodes[3].referencedElementCode).isEqualTo("d10")
                        assertThat(it.childNodes.size).isEqualTo(4)
                    }

                    7 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Lei)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("l11")
                        assertThat(it.childNodes).isEmpty()
                    }

                    8 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Percentage)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("p8")
                        assertThat(it.childNodes).isEmpty()
                    }

                    9 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Isin)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("i12")
                        assertThat(it.childNodes).isEmpty()
                    }

                    10 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Decimal)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("d10")
                        assertThat(it.childNodes).isEmpty()
                    }

                    11 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "MET hierarchy node (Integer)")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("i6")
                        assertThat(it.childNodes).isEmpty()
                    }
                }
            }
        }

        @Test
        fun `1 HierarchyNode within 'METHIER' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().metricDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "METHIER" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(1)

            hierarchy.allNodes().forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/metrics-2018-1/extension/METHIER/member/1")
                        assertThat(it.type).isEqualTo("HierarchyNode")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isNull()
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Metric hierarchy node (fi, label)")
                        )

                        assertThat(it.concept.description.translations).isEmpty()

                        assertThat(it.abstract).isFalse()
                        assertThat(it.comparisonOperator).isNull()
                        assertThat(it.unaryOperator).isNull()

                        assertThat(it.referencedElementCode).isEqualTo("b1")
                        assertThat(it.childNodes).isEmpty()
                    }
                }
            }
        }
    }
}
