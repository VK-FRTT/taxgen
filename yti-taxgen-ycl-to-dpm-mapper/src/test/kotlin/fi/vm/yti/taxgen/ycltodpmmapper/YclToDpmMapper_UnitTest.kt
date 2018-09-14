package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.testcommons.DiagnosticConsumerCaptorSimple
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.YCL_SOURCE_CAPTURE
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.yclsourceprovider.folder.YclSourceFolderStructureAdapter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Mapping YCL sources to DPM model")
internal class YclToDpmMapper_UnitTest {

    private lateinit var diagnosticConsumerCaptor: DiagnosticConsumerCaptorSimple
    private lateinit var diagnostic: Diagnostic

    private lateinit var yclSource: YclSource
    private lateinit var yclToDpmMapper: YclToDpmMapper

    @AfterEach
    fun teardown() {
        yclSource.close()
    }

    fun performMapping(): List<DpmDictionary> {
        diagnosticConsumerCaptor = DiagnosticConsumerCaptorSimple()
        diagnostic = DiagnosticBridge(diagnosticConsumerCaptor)

        yclToDpmMapper = YclToDpmMapper(diagnostic)

        return yclToDpmMapper.getDpmDictionariesFromSource(
            yclSource = yclSource
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
            yclSource = createYclSourceFromTestFixture("codelist_comprehensive")
            //codelist_comprehensive

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
        fun `should produce Explicit Domain with correct DomainCode`() {
            val domainCode = performMapping()[0].explicitDomains[0].domainCode

            assertThat(domainCode).isEqualTo("tf_dc_override")
        }

        @Test
        fun `should produce Explicit Domain with 3 Members`() {
            val members = performMapping()[0].explicitDomains[0].members

            assertThat(members.size).isEqualTo(3)
        }

        @Test
        fun `should produce 1st Member with correct Concept, MemberCode and DefaultCode values`() {
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

                assertThat(description).isNotNull()

                assertThat(description.translations).containsOnly(
                    entry(en, "Code 0 description #en"),
                    entry(fi, "Code 0 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("code_0")

            assertThat(member.defaultMember).isFalse()
        }

        @Test
        fun `should produce 2nd Member with correct Concept, MemberCode and DefaultCode values`() {
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

                assertThat(description).isNotNull()
                assertThat(description.translations).containsOnly(
                    entry(en, "Code 1 description #en"),
                    entry(fi, "Code 1 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("code_1")

            assertThat(member.defaultMember).isTrue()
        }

        @Test
        fun `should produce 3rd Member with correct Concept, MemberCode and DefaultCode values`() {
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

                assertThat(description).isNotNull()
                assertThat(description.translations).containsOnly(
                    entry(en, "Code 2 description #en"),
                    entry(fi, "Code 2 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("code_2")

            assertThat(member.defaultMember).isFalse()
        }

        @Test
        fun `should produce correct diagnostic context events`() {
            performMapping()
            assertThat(diagnosticConsumerCaptor.events).containsExactly(
                "ENTER [ActivityMapYclToDpm]",
                "ENTER [YclSource]",
                "ENTER [DpmDictionary]",
                "ENTER [DpmOwner]",
                "UPDATE [DpmOwner] ORIGINAL [DpmOwner]",
                "EXIT [DpmDictionary] RETIRED [DpmOwner]",
                "UPDATE [DpmDictionary] ORIGINAL [DpmDictionary]",
                "ENTER [YclCodelist]",
                "UPDATE [YclCodelist] ORIGINAL [YclCodelist]",
                "ENTER [YclCode]",
                "EXIT [YclCodelist] RETIRED [YclCode]",
                "ENTER [YclCode]",
                "EXIT [YclCodelist] RETIRED [YclCode]",
                "ENTER [YclCode]",
                "EXIT [YclCodelist] RETIRED [YclCode]",
                "EXIT [DpmDictionary] RETIRED [YclCodelist]",
                "EXIT [YclSource] RETIRED [DpmDictionary]",
                "EXIT [ActivityMapYclToDpm] RETIRED [YclSource]",
                "EXIT [] RETIRED [ActivityMapYclToDpm]"
            )
        }
    }

    @Nested
    @DisplayName("when source defines memberCodePrefix")
    inner class MemberCodePrefixSource {

        @BeforeEach
        fun init() {
            yclSource = createYclSourceFromTestFixture("codelist_minimal_member_code_prefix")
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
            yclSource = createYclSourceFromTestFixture("dpm_dictionaries_3")
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
            yclSource = createYclSourceFromTestFixture("ycl_codepages_disordered")
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
            yclSource = createYclSourceFromTestFixture("invalid/owner_info_json_broken")
            performMapping()
        }

        @Test
        fun `Missing Owner name should cause validation error`() {
            yclSource = createYclSourceFromTestFixture("invalid/owner_name_missing")
            performMapping()
        }

        @Test
        fun `Unsupported Owner language should cause mapping error`() {
            yclSource = createYclSourceFromTestFixture("invalid/owner_language_unsupported")
            performMapping()
        }

        @Test
        fun `Empty Owner name should cause data validation error`() {
            yclSource = createYclSourceFromTestFixture("invalid/owner_name_empty")
            performMapping()
        }
    }

    @Disabled
    @Nested
    @DisplayName("with invalid YCL CodeScheme data source")
    inner class InvalidYclCodeSchemeDataSource {

        @Test
        fun `Broken CodeScheme JSON should cause mapping error`() {
            yclSource = createYclSourceFromTestFixture("invalid/codescheme_json_broken")
            performMapping()
        }

        @Test
        fun `Missing default code should cause data validation error`() {
            yclSource = createYclSourceFromTestFixture("invalid/codescheme_missing_default_code")
            performMapping()
        }
    }

    @Disabled
    @Nested
    @DisplayName("with invalid YCL CodesPage data source")
    inner class InvalidYclCodesPageDataSource {

        @Test
        fun `Broken CodesPage JSON should cause mapping error`() {
            yclSource = createYclSourceFromTestFixture("invalid/")
            performMapping()
        }
    }

    private fun createYclSourceFromTestFixture(fixtureName: String): YclSource {
        val fixturePath = TestFixture.pathOf(YCL_SOURCE_CAPTURE, fixtureName)
        return YclSourceFolderStructureAdapter(fixturePath)
    }
}
