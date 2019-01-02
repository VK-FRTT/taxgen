package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.YCL_SOURCE_CAPTURE
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsprovider.folder.DpmSourceFolderAdapter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Mapping YCL sources to DPM model")
internal class RdsToDpmMapper_UnitTest {

    private lateinit var diagnosticCollector: DiagnosticCollectorSimple
    private lateinit var diagnostic: Diagnostic

    private lateinit var dpmSource: DpmSource
    private lateinit var yclToDpmMapper: RdsToDpmMapper

    @AfterEach
    fun teardown() {
        dpmSource.close()
    }

    fun performMapping(): List<DpmDictionary> {
        diagnosticCollector = DiagnosticCollectorSimple()
        diagnostic = DiagnosticBridge(diagnosticCollector)

        yclToDpmMapper = RdsToDpmMapper(diagnostic)

        return yclToDpmMapper.extractDpmDictionariesFromSource(
            dpmSource = dpmSource
        )
    }

    @Nested
    @DisplayName("when source provides comprehensively YCL source values")
    inner class SingleComprehensiveSource {

        private lateinit var en: Language
        private lateinit var fi: Language
        private lateinit var sv: Language

        @BeforeEach
        fun init() {
            dpmSource = createYclSourceFromTestFixture("codelist_comprehensive")

            en = Language.findByIso6391Code("en")!!
            fi = Language.findByIso6391Code("fi")!!
            sv = Language.findByIso6391Code("sv")!!
        }

        @Test
        fun `should produce 1 DPM Dictionary`() {
            val dpmDictionaries = performMapping()

            assertThat(dpmDictionaries.size).isEqualTo(1)
        }

        @Test
        fun `should produce DPM Dictionary with correct Owner`() {
            val owner = performMapping()[0].owner

            assertThat(owner.name).isEqualTo("ComprehensiveName")
            assertThat(owner.namespace).isEqualTo("ComprehensiveNamespace")
            assertThat(owner.prefix).isEqualTo("ComprehensivePrefix")
            assertThat(owner.location).isEqualTo("ComprehensiveLocation")
            assertThat(owner.copyright).isEqualTo("ComprehensiveCopyright")
            assertThat(owner.languages).isEqualTo(hashSetOf(en, fi, sv))
            assertThat(owner.defaultLanguage).isEqualTo(en)
        }

        @Test
        fun `should produce DPM Dictionary with 1 Explicit Domain`() {
            val dpmDictionary = performMapping()[0]

            assertThat(dpmDictionary.explicitDomains.size).isEqualTo(1)
        }

        @Test
        fun `should produce Explicit Domain with correct Concept`() {
            val concept = performMapping()[0].explicitDomains[0].concept

            assertThat(concept.createdAt).isAfter("2018-09-14T00:00:00.000Z")
            assertThat(concept.modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

            assertThat(concept.applicableFrom).isNull()
            assertThat(concept.applicableUntil).isNull()

            assertThat(concept.label.translations).containsOnly(
                entry(en, "Test Fixture: Codelist Comprehensive #en"),
                entry(fi, "Test Fixture: Codelist Comprehensive #fi")
            )

            assertThat(concept.description).isNotNull()
            assertThat(concept.description.translations).containsOnly(
                entry(en, "Description #en"),
                entry(fi, "Description #fi")
            )
        }

        @Test
        fun `should produce Explicit Domain with correct identifiers`() {
            val explicitDomain = performMapping()[0].explicitDomains[0]

            assertThat(explicitDomain.domainCode).isEqualTo("tf_dc_override")
            assertThat(explicitDomain.id).isEqualTo("5314a353-5ad4-4c81-8368-70495979e3c4")
            assertThat(explicitDomain.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive")
            assertThat(explicitDomain.type).isEqualTo("ExplicitDomain.kt")
        }

        @Test
        fun `should produce Explicit Domain with 5 Members and 2 hierarchies`() {
            val explicitDomain = performMapping()[0].explicitDomains[0]

            assertThat(explicitDomain.members.size).isEqualTo(5)
            assertThat(explicitDomain.hierarchies.size).isEqualTo(2)
        }

        @Nested
        @DisplayName("with domain members")
        inner class DomainMembers {

            @Test
            fun `should produce 1st Member with correct concept, default member status & identifiers`() {
                val member = performMapping()[0].explicitDomains[0].members[0]

                member.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Code 0 #en"),
                        entry(fi, "Code 0 #fi")
                    )

                    assertThat(description.translations).containsOnly(
                        entry(en, "Code 0 description #en"),
                        entry(fi, "Code 0 description #fi")
                    )
                }

                assertThat(member.memberCode).isEqualTo("code_0")
                assertThat(member.defaultMember).isFalse()

                assertThat(member.id).isEqualTo("eb81fdea-6c24-4e74-b159-f34a8d9dc476")
                assertThat(member.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_0")
                assertThat(member.type).isEqualTo("Member")
            }

            @Test
            fun `should produce 2nd Member with correct concept, default member status & identifiers`() {
                val member = performMapping()[0].explicitDomains[0].members[1]

                member.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Code 1 #en"),
                        entry(fi, "Code 1 #fi")
                    )

                    assertThat(description.translations).containsOnly(
                        entry(en, "Code 1 description #en"),
                        entry(fi, "Code 1 description #fi")
                    )
                }

                assertThat(member.memberCode).isEqualTo("code_1")
                assertThat(member.defaultMember).isTrue()

                assertThat(member.id).isEqualTo("2f13f036-21b9-4855-9457-a869fffd5bb8")
                assertThat(member.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_1")
                assertThat(member.type).isEqualTo("Member")
            }

            @Test
            fun `should produce 3rd Member with correct concept, default member status & identifiers`() {
                val member = performMapping()[0].explicitDomains[0].members[2]

                member.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Code 2 #en"),
                        entry(fi, "Code 2 #fi")
                    )

                    assertThat(description.translations).containsOnly(
                        entry(en, "Code 2 description #en"),
                        entry(fi, "Code 2 description #fi")
                    )
                }

                assertThat(member.defaultMember).isFalse()
                assertThat(member.memberCode).isEqualTo("code_2")

                assertThat(member.id).isEqualTo("ea41b4e5-9551-428a-8e3f-45d85b9b326e")
                assertThat(member.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_2")
                assertThat(member.type).isEqualTo("Member")
            }
        }

        @Nested
        @DisplayName("with definition hierarchy")
        inner class DefinitionHierarchy {

            @Test
            fun `should produce 'def_hier_0' with total 5 nodes`() {
                val hierarchy =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "def_hier_0" }
                assertThat(hierarchy.allNodes().size).isEqualTo(5)
            }

            @Test
            fun `should produce 'def_hier_0' hierarchy with correct structure`() {
                val rootNodes =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "def_hier_0" }
                        .rootNodes

                rootNodes[0].apply {
                    assertThat(memberRef.uri).endsWith("/code/code_0")
                    assertThat(childNodes!!.size).isEqualTo(1)

                    childNodes!![0].apply {
                        assertThat(memberRef.uri).endsWith("/code/code_1")
                        assertThat(childNodes!!.size).isEqualTo(1)

                        childNodes!![0].apply {
                            assertThat(memberRef.uri).endsWith("/code/code_2")
                            assertThat(childNodes!!.size).isEqualTo(0)
                        }
                    }
                }

                rootNodes[1].apply {
                    assertThat(memberRef.uri).endsWith("/code/code_3")
                    assertThat(childNodes!!.size).isEqualTo(1)

                    childNodes!![0].apply {
                        assertThat(memberRef.uri).endsWith("/code/code_4")
                        assertThat(childNodes!!.size).isEqualTo(0)
                    }
                }
            }

            @Test
            fun `should produce 'def_hier_0' hierarchy with correct 1st Node`() {
                val node =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "def_hier_0" }.rootNodes[0]

                node.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Definition member 0 #en"),
                        entry(fi, "Definition member 0 #fi")
                    )

                    assertThat(description.translations).isEmpty()
                }

                assertThat(node.abstract).isFalse()
                assertThat(node.comparisonOperator).isNull()
                assertThat(node.unaryOperator).isNull()
                assertThat(node.memberRef.id).isEqualTo("eb81fdea-6c24-4e74-b159-f34a8d9dc476")
                assertThat(node.memberRef.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_0")
                assertThat(node.memberRef.type).isEqualTo("Member")

                assertThat(node.id).isEqualTo("bafd19a7-4bf0-434a-ad2d-a891f26016d9")
                assertThat(node.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/extension/def_hier_0/member/bafd19a7-4bf0-434a-ad2d-a891f26016d9")
                assertThat(node.type).isEqualTo("HierarchyNode")
            }

            @Test
            fun `should produce 'def_hier_0' hierarchy with correct 2nd Node`() {
                val node =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "def_hier_0" }.rootNodes[0].childNodes!![0]

                node.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Definition member 1 #en"),
                        entry(fi, "Definition member 1 #fi")
                    )

                    assertThat(description.translations).isEmpty()
                }

                assertThat(node.abstract).isFalse()
                assertThat(node.comparisonOperator).isNull()
                assertThat(node.unaryOperator).isNull()
                assertThat(node.memberRef.id).isEqualTo("2f13f036-21b9-4855-9457-a869fffd5bb8")
                assertThat(node.memberRef.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_1")
                assertThat(node.memberRef.type).isEqualTo("Member")

                assertThat(node.id).isEqualTo("50c530d9-06ea-4680-99c4-bd150a8586a7")
                assertThat(node.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/extension/def_hier_0/member/50c530d9-06ea-4680-99c4-bd150a8586a7")
                assertThat(node.type).isEqualTo("HierarchyNode")
            }
        }

        @Nested
        @DisplayName("with calculation hierarchy")
        inner class CalculationHierarchy {

            @Test
            fun `should produce 'calc_hier_0' with total 5 nodes`() {
                val hierarchy =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "calc_hier_0" }
                assertThat(hierarchy.allNodes().size).isEqualTo(5)
            }

            @Test
            fun `should produce 'calc_hier_0' hierarchy with correct structure`() {
                val rootNodes =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "calc_hier_0" }
                        .rootNodes

                rootNodes[0].apply {
                    assertThat(memberRef.uri).endsWith("/code/code_0")
                    assertThat(childNodes!!.size).isEqualTo(1)

                    childNodes!![0].apply {
                        assertThat(memberRef.uri).endsWith("/code/code_1")
                        assertThat(childNodes!!.size).isEqualTo(1)

                        childNodes!![0].apply {
                            assertThat(memberRef.uri).endsWith("/code/code_2")
                            assertThat(childNodes!!.size).isEqualTo(0)
                        }
                    }
                }

                rootNodes[1].apply {
                    assertThat(memberRef.uri).endsWith("/code/code_3")
                    assertThat(childNodes!!.size).isEqualTo(1)

                    childNodes!![0].apply {
                        assertThat(memberRef.uri).endsWith("/code/code_4")
                        assertThat(childNodes!!.size).isEqualTo(0)
                    }
                }
            }

            @Test
            fun `should produce 'calc_hier_0' hierarchy with correct 1st Node`() {
                val node =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "calc_hier_0" }.rootNodes[0]

                node.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Calculation member 0 #en"),
                        entry(fi, "Calculation member 0 #fi")
                    )

                    assertThat(description.translations).isEmpty()
                }

                assertThat(node.abstract).isFalse()
                assertThat(node.comparisonOperator).isEqualTo("=")
                assertThat(node.unaryOperator).isEqualTo("+")
                assertThat(node.memberRef.id).isEqualTo("eb81fdea-6c24-4e74-b159-f34a8d9dc476")
                assertThat(node.memberRef.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_0")
                assertThat(node.memberRef.type).isEqualTo("Member")

                assertThat(node.id).isEqualTo("d9f3ab1f-fc31-466f-8085-058673122a86")
                assertThat(node.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/extension/calc_hier_0/member/d9f3ab1f-fc31-466f-8085-058673122a86")
                assertThat(node.type).isEqualTo("HierarchyNode")
            }

            @Test
            fun `should produce 'calc_hier_0' hierarchy with correct 2nd Node`() {
                val node =
                    performMapping()[0].explicitDomains[0].hierarchies.first { it.hierarchyCode == "calc_hier_0" }.rootNodes[0].childNodes!![0]

                node.concept.apply {
                    assertThat(createdAt).isAfter("2018-09-14T00:00:00.000Z")
                    assertThat(modifiedAt).isAfter("2018-09-14T00:00:00.000Z")

                    assertThat(applicableFrom).isNull()
                    assertThat(applicableUntil).isNull()

                    assertThat(label.translations).containsOnly(
                        entry(en, "Calculation member 1 #en"),
                        entry(fi, "Calculation member 1 #fi")
                    )

                    assertThat(description.translations).isEmpty()
                }

                assertThat(node.abstract).isFalse()
                assertThat(node.comparisonOperator).isEqualTo("=")
                assertThat(node.unaryOperator).isEqualTo("+")
                assertThat(node.memberRef.id).isEqualTo("2f13f036-21b9-4855-9457-a869fffd5bb8")
                assertThat(node.memberRef.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/code/code_1")
                assertThat(node.memberRef.type).isEqualTo("Member")

                assertThat(node.id).isEqualTo("067a9156-b657-4e5c-9bf6-35f7da4a4274")
                assertThat(node.uri).isEqualTo("http://uri.suomi.fi/codelist/yti-xbrl/testfixture_codelist_comprehensive/extension/calc_hier_0/member/067a9156-b657-4e5c-9bf6-35f7da4a4274")
                assertThat(node.type).isEqualTo("HierarchyNode")
            }
        }

        @Test
        fun `should produce correct diagnostic context events`() {
            performMapping()
            assertThat(diagnosticCollector.events).containsExactly(
                "ENTER [ActivityMapYclToDpm]",
                "ENTER [DpmSource]",
                "ENTER [DpmDictionary]",
                "ENTER [DpmOwner]",
                "UPDATE [DpmOwner]",
                "EXIT [DpmOwner]",
                "UPDATE [DpmDictionary]",
                "ENTER [YclCodelist]",
                "UPDATE [YclCodelist]",
                "ENTER [RdsCode]",
                "EXIT [RdsCode]",
                "ENTER [RdsCode]",
                "EXIT [RdsCode]",
                "ENTER [RdsCode]",
                "EXIT [RdsCode]",
                "ENTER [RdsCode]",
                "EXIT [RdsCode]",
                "ENTER [RdsCode]",
                "EXIT [RdsCode]",
                "ENTER [YclCodelistExtension]",
                "UPDATE [YclCodelistExtension]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "EXIT [YclCodelistExtension]",
                "ENTER [YclCodelistExtension]",
                "UPDATE [YclCodelistExtension]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "ENTER [RdsExtensionMember]",
                "EXIT [RdsExtensionMember]",
                "EXIT [YclCodelistExtension]",
                "EXIT [YclCodelist]",
                "EXIT [DpmDictionary]",
                "EXIT [DpmSource]",
                "EXIT [ActivityMapYclToDpm]"
            )
        }
    }

    @Disabled
    @Nested
    @DisplayName("when source defines memberCodePrefix")
    inner class MemberCodePrefixSource {

        @BeforeEach
        fun init() {
            dpmSource = createYclSourceFromTestFixture("codelist_minimal_member_code_prefix")
        }

        @Test
        fun `should produce Member with prefixed MemberCode`() {
            val member = performMapping()[0].explicitDomains[0].members[0]

            assertThat(member.memberCode).startsWith("mc_prefix_")
            assertThat(member.memberCode).endsWith("code_0")

            assertThat(member.memberCode).isEqualTo("mc_prefix_code_0")
        }
    }

    @Disabled
    @Nested
    @DisplayName("when source defines 3 Dpm Dictionaries")
    inner class ThreeDpmDictionariesSource {

        @BeforeEach
        fun init() {
            dpmSource = createYclSourceFromTestFixture("dpm_dictionaries_3")
        }

        @Test
        fun `Mapping result should have 3 DPM Dictionaries`() {
            val dpmDictionaries = performMapping()
            assertThat(dpmDictionaries.size).isEqualTo(3)
        }

        @Test
        fun `1st DPM Dictionary should have correct Owner`() {
            val owner = performMapping()[0].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_0_Name")
        }

        @Test
        fun `2nd DPM Dictionary should have correct Owner`() {
            val owner = performMapping()[1].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_1_Name")
        }

        @Test
        fun `3rd DPM Dictionary should have correct Owner`() {
            val owner = performMapping()[2].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_2_Name")
        }
    }

    @Disabled
    @Nested
    @DisplayName("when source provides disordered codepages")
    inner class DisorderedCodepagesSource {

        @BeforeEach
        fun init() {
            dpmSource = createYclSourceFromTestFixture("ycl_codepages_disordered")
        }

        @Test
        fun `Dictionary should have correct Owner`() {
            val owner = performMapping()[0].owner
            assertThat(owner.name).isEqualTo("DisorderedYclCodepages_Name")
        }

        @Test
        fun `Explicit Domain should have 6 Members`() {
            val members = performMapping()[0].explicitDomains[0].members

            assertThat(members.size).isEqualTo(6)
        }

        @Test
        fun `Members should be in correct order`() {
        }
    }

    @Disabled
    @Nested
    @DisplayName("from invalid owner data source")
    inner class InvalidOwnerDataSource {

        @Test
        fun `Broken Owner Info JSON should cause mapping error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/owner_config_json_broken")
            performMapping()
        }

        @Test
        fun `Missing Owner name should cause validation error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/owner_name_missing")
            performMapping()
        }

        @Test
        fun `Unsupported Owner language should cause mapping error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/owner_language_unsupported")
            performMapping()
        }

        @Test
        fun `Empty Owner name should cause data validation error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/owner_name_empty")
            performMapping()
        }
    }

    @Disabled
    @Nested
    @DisplayName("with invalid YCL CodeScheme data source")
    inner class InvalidYclCodeSchemeDataSource {

        @Test
        fun `Broken CodeScheme JSON should cause mapping error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/codescheme_json_broken")
            performMapping()
        }

        @Test
        fun `Missing default code should cause data validation error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/codescheme_missing_default_code")
            performMapping()
        }
    }

    @Disabled
    @Nested
    @DisplayName("with invalid YCL CodesPage data source")
    inner class InvalidYclCodesPageDataSource {

        @Test
        fun `Broken CodesPage JSON should cause mapping error`() {
            dpmSource = createYclSourceFromTestFixture("invalid/")
            performMapping()
        }
    }

    private fun createYclSourceFromTestFixture(fixtureName: String): DpmSource {
        val fixturePath = TestFixture.pathOf(YCL_SOURCE_CAPTURE, fixtureName)
        return DpmSourceFolderAdapter(fixturePath)
    }
}
