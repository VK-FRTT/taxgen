package fi.vm.yti.taxgen.rdsdpmmapper

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

internal class RdsToDpmMapper_IntegrationFixtureContent_ExplicitDomainsAndHierarchies_ModuleTest
    : RdsToDpmMapper_ModuleTestBase() {

    @Test
    fun `7 Explicit Domains`() {
        val dpmDictionary = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture()

        assertThat(dpmDictionary.explicitDomains.size).isEqualTo(7)

        dpmDictionary.explicitDomains.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    assertThat(it.domainCode).isEqualTo("DOME")
                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-doms-2018-1/code/DOME")
                    assertThat(it.type).isEqualTo("ExplicitDomain")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-12-31")
                    assertThat(it.concept.applicableUntil).isEqualTo("2019-05-30")

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain (fi, label)")
                    )

                    assertThat(it.concept.description.translations).containsOnly(
                        entry(fi, "Explicit domain (fi, description)")
                    )
                }

                1 -> {
                    assertThat(it.domainCode).isEqualTo("EDA")
                    assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/exp-doms-2018-1/code/EDA")
                    assertThat(it.type).isEqualTo("ExplicitDomain")

                    assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                    assertThat(it.concept.applicableUntil).isNull()

                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A")
                    )

                    assertThat(it.concept.description.translations).isEmpty()
                }

                2 -> {
                    assertThat(it.domainCode).isEqualTo("EDA1")
                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A1")
                    )
                }

                3 -> {
                    assertThat(it.domainCode).isEqualTo("EDA9")
                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A9")
                    )
                }

                4 -> {
                    assertThat(it.domainCode).isEqualTo("EDA10")
                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A10")
                    )
                }

                5 -> {
                    assertThat(it.domainCode).isEqualTo("EDA11")
                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A11")
                    )
                }

                6 -> {
                    assertThat(it.domainCode).isEqualTo("EDA20")
                    assertThat(it.concept.label.translations).containsOnly(
                        entry(fi, "Explicit domain A20")
                    )
                }

                else -> {
                    fail { "Unexpected item" }
                }
            }
        }
    }

    @Nested
    inner class ExplicitDomain_DOME {

        private val domainCode = "DOME"

        @Test
        fun `2 Members`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!

            assertThat(domain.members.size).isEqualTo(2)

            domain.members.forEachIndexed { index, it ->
                when (index) {

                    0 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x1")
                        assertThat(it.defaultMember).isFalse()

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/DOME-2018-1/code/EDA-x1")
                        assertThat(it.type).isEqualTo("Member")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-12-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "DOME member duplicating EDA code")
                        )
                    }

                    1 -> {
                        assertThat(it.memberCode).isEqualTo("MEM")
                        assertThat(it.defaultMember).isFalse()

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/DOME-2018-1/code/MEM")
                        assertThat(it.type).isEqualTo("Member")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Member (fi, label)")
                        )

                        assertThat(it.concept.description.translations).containsOnly(
                            entry(fi, "Member (fi, description)")
                        )
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `2 Hierarchies`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!

            assertThat(domain.hierarchies.size).isEqualTo(2)

            domain.hierarchies.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.hierarchyCode).isEqualTo("EDA-H1")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/DOME-2018-1/extension/EDA-H1")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-12-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "DOME hierarchy duplicating EDA code")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    1 -> {
                        assertThat(it.hierarchyCode).isEqualTo("HIER")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/DOME-2018-1/extension/HIER")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Hierarchy (fi, label)")
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
        fun `1 HierarchyNode within 'HIER' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "HIER" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(1)

            hierarchy.rootNodes.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/DOME-2018-1/extension/HIER/member/1")
                        assertThat(it.type).isEqualTo("HierarchyNode")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isNull()
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "Hierarchy node (fi, label)")
                        )

                        assertThat(it.concept.description.translations).isEmpty()

                        assertThat(it.abstract).isFalse()
                        assertThat(it.comparisonOperator).isNull()
                        assertThat(it.unaryOperator).isNull()

                        assertThat(it.referencedElementCode).isEqualTo("MEM")
                        assertThat(it.childNodes).isEmpty()
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `0 HierarchyNode within 'EDA-H1' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "EDA-H1" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(0)
        }
    }

    @Nested
    inner class ExplicitDomain_EDA {

        private val domainCode = "EDA"

        @Test
        fun `13 Members`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!

            assertThat(domain.members.size).isEqualTo(13)

            domain.members.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x1")
                        assertThat(it.defaultMember).isFalse()

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/code/EDA-x1")
                        assertThat(it.type).isEqualTo("Member")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA member 1")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    1 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x2")
                    }

                    2 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x3")
                        assertThat(it.defaultMember).isTrue()
                    }

                    3 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x4")
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA member (=, 1)")
                        )
                    }

                    4 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x5")
                    }

                    5 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x6")
                    }

                    6 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x7")
                    }

                    7 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x8")
                    }

                    8 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x9")
                    }

                    9 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x10")
                    }

                    10 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x19")
                    }

                    11 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x20")
                    }

                    12 -> {
                        assertThat(it.memberCode).isEqualTo("EDA-x21")
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `3 Hierarchies`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!

            assertThat(domain.hierarchies.size).isEqualTo(3)

            domain.hierarchies.forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.hierarchyCode).isEqualTo("EDA-H1")

                        assertThat(it.uri).isEqualTo("http://uri.suomi.fi/codelist/dpm-integration-fixture/EDA-2018-1/extension/EDA-H1")
                        assertThat(it.type).isEqualTo("Hierarchy")

                        assertThat(it.concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
                        assertThat(it.concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                        assertThat(it.concept.applicableFrom).isEqualTo("2018-10-31")
                        assertThat(it.concept.applicableUntil).isNull()

                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy 1")
                        )

                        assertThat(it.concept.description.translations).isEmpty()
                    }

                    1 -> {
                        assertThat(it.hierarchyCode).isEqualTo("EDA-H2")
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy 2")
                        )
                    }

                    2 -> {
                        assertThat(it.hierarchyCode).isEqualTo("EDA-H10")
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy 10")
                        )
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `8 HierarchyNodes within 'EDA-H1' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "EDA-H1" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(8)

            hierarchy.allNodes().forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 7")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x20")
                        assertThat(it.childNodes).isEmpty()
                    }

                    1 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 4")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x9")

                        assertThat(it.childNodes[0].referencedElementCode).isEqualTo("EDA-x10")
                        assertThat(it.childNodes.size).isEqualTo(1)
                    }

                    2 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 5")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x10")
                        assertThat(it.childNodes).isEmpty()
                    }

                    3 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 6")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x19")
                        assertThat(it.childNodes).isEmpty()
                    }

                    4 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 2")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x2")
                        assertThat(it.childNodes[0].referencedElementCode).isEqualTo("EDA-x1")
                        assertThat(it.childNodes[1].referencedElementCode).isEqualTo("EDA-x3")
                        assertThat(it.childNodes.size).isEqualTo(2)
                    }

                    5 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 1")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x1")
                        assertThat(it.childNodes).isEmpty()
                    }

                    6 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 3")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x3")
                        assertThat(it.childNodes).isEmpty()
                    }

                    7 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node 8")
                        )

                        assertThat(it.referencedElementCode).isEqualTo("EDA-x21")
                        assertThat(it.childNodes).isEmpty()
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `5 HierarchyNodes (with operators) within 'EDA-H2' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "EDA-H2" }!!

            assertThat(hierarchy.allNodes().size).isEqualTo(5)

            hierarchy.allNodes().forEachIndexed { index, it ->
                when (index) {
                    0 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node (=, 1)")
                        )
                        assertThat(it.unaryOperator).isEqualTo("+")
                        assertThat(it.comparisonOperator).isEqualTo("=")
                        assertThat(it.referencedElementCode).isEqualTo("EDA-x4")
                    }

                    1 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node (>, 2)")
                        )
                        assertThat(it.unaryOperator).isNull()
                        assertThat(it.comparisonOperator).isEqualTo(">")
                        assertThat(it.referencedElementCode).isEqualTo("EDA-x5")
                    }

                    2 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node (<, 0)")
                        )
                        assertThat(it.unaryOperator).isNull()
                        assertThat(it.comparisonOperator).isEqualTo("<")
                        assertThat(it.referencedElementCode).isEqualTo("EDA-x6")
                    }

                    3 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node (>=, -1)")
                        )
                        assertThat(it.unaryOperator).isEqualTo("-")
                        assertThat(it.comparisonOperator).isEqualTo(">=")
                        assertThat(it.referencedElementCode).isEqualTo("EDA-x7")
                    }

                    4 -> {
                        assertThat(it.concept.label.translations).containsOnly(
                            entry(fi, "EDA hierarchy node (<=, -2)")
                        )
                        assertThat(it.unaryOperator).isNull()
                        assertThat(it.comparisonOperator).isEqualTo("<=")
                        assertThat(it.referencedElementCode).isEqualTo("EDA-x8")
                    }

                    else -> {
                        fail { "Unexpected item" }
                    }
                }
            }
        }

        @Test
        fun `0 HierarchyNodes within 'EDA-H19' hierarchy`() {
            val domain = executeRdsToDpmMapperAndGetDictionariesFromIntegrationFixture().explicitDomains.find { it.domainCode == domainCode }!!
            val hierarchy = domain.hierarchies.find { it.hierarchyCode == "EDA-H10" }!!

            assertThat(hierarchy.allNodes()).isEmpty()
        }
    }
}
