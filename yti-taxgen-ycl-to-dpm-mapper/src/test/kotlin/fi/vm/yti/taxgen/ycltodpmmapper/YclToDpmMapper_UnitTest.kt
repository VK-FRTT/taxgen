package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.testcommons.TestFixture
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
import java.time.Instant

@DisplayName("When YCL sources are mapped to DPM model")
internal class YclToDpmMapper_UnitTest {

    private lateinit var yclSource: YclSource
    private val yclToDpmMapper = YclToDpmMapper()

    @AfterEach
    fun teardown() {
        yclSource.close()
    }

    @Nested
    @DisplayName("and single comprehensive YCL source is used")
    inner class SingleSourceElements {

        private lateinit var en: Language
        private lateinit var fi: Language
        private lateinit var sv: Language

        @BeforeEach
        fun init() {
            yclSource = createYclSourceFromTestFixture("single_comprehensive_tree")

            en = Language.findByIso6391Code("en")!!
            fi = Language.findByIso6391Code("fi")!!
            sv = Language.findByIso6391Code("sv")!!
        }

        @Test
        fun `Mapping result should have 1 DPM Dictionary`() {
            val dpmDictionaries = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)

            assertThat(dpmDictionaries.size).isEqualTo(1)
        }

        @Test
        fun `DPM Dictionary should have correct Owner`() {
            val owner = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].owner

            assertThat(owner.name).isEqualTo("SingleComprehensiveTree_Name")
            assertThat(owner.namespace).isEqualTo("SingleComprehensiveTree_Namespace")
            assertThat(owner.prefix).isEqualTo("sct_prefix")
            assertThat(owner.location).isEqualTo("SingleComprehensiveTree_Location")
            assertThat(owner.copyright).isEqualTo("SingleComprehensiveTree_Copyright")
            assertThat(owner.languages).isEqualTo(listOf(en, fi, sv))
            assertThat(owner.defaultLanguage).isEqualTo(en)
        }

        @Test
        fun `DPM Dictionary should have 1 Explicit Domain`() {
            val dpmDictionary = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0]

            assertThat(dpmDictionary.explicitDomains.size).isEqualTo(1)
        }

        @Test
        fun `Explicit Domain should have correct Concept`() {
            val concept = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].concept

            assertThat(concept.createdAt).isEqualTo(Instant.parse("2018-06-26T14:53:37.664Z"))
            assertThat(concept.modifiedAt).isEqualTo(Instant.parse("2018-06-26T14:54:37.664Z"))

            assertThat(concept.applicableFrom).isNull()
            assertThat(concept.applicableUntil).isNull()

            assertThat(concept.label.translations).containsOnly(
                entry(en, "Test fixture: Comprehensive codelist #en"),
                entry(fi, "Test fixture: Comprehensive codelist #fi")
            )

            assertThat(concept.description).isNotNull()
            assertThat(concept.description.translations).containsOnly(
                entry(en, "Comprehensive description #en"),
                entry(fi, "Comprehensive description #fi")
            )
        }

        @Test
        fun `Explicit Domain should have correct DomainCode`() {
            val domainCode = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].domainCode

            assertThat(domainCode).isEqualTo("tf_comprehensive_codelist")
        }

        @Test
        fun `Explicit Domain should have 3 Members`() {
            val members = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].members

            assertThat(members.size).isEqualTo(3)
        }

        @Test
        fun `1st Member should have correct Concept, MemberCode and DefaultCode values`() {
            val member = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].members[0]

            member.concept.apply {
                assertThat(createdAt).isEqualTo(Instant.parse("2018-06-26T14:53:37.689Z"))
                assertThat(modifiedAt).isEqualTo(Instant.parse("2018-06-26T14:54:37.689Z"))

                assertThat(applicableFrom).isNull()
                assertThat(applicableUntil).isNull()

                assertThat(label.translations).containsOnly(
                    entry(en, "Comprehensive code 0 #en"),
                    entry(fi, "Comprehensive code 0 #fi")
                )

                assertThat(description).isNotNull()

                assertThat(description.translations).containsOnly(
                    entry(en, "Code 0 description #en"),
                    entry(fi, "Code 0 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("tf_cmpr_cl_code0")

            assertThat(member.defaultMember).isFalse()
        }

        @Test
        fun `2nd Member should have correct Concept, MemberCode and DefaultCode values`() {
            val member = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].members[1]

            member.concept.apply {
                assertThat(createdAt).isEqualTo(Instant.parse("2018-06-26T14:53:37.706Z"))
                assertThat(modifiedAt).isEqualTo(Instant.parse("2018-06-26T14:54:37.706Z"))

                assertThat(applicableFrom).isNull()
                assertThat(applicableUntil).isNull()

                assertThat(label.translations).containsOnly(
                    entry(en, "Comprehensive code 1 #en"),
                    entry(fi, "Comprehensive code 1 #fi")
                )

                assertThat(description).isNotNull()
                assertThat(description.translations).containsOnly(
                    entry(en, "Code 1 description #en"),
                    entry(fi, "Code 1 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("tf_cmpr_cl_code1")

            assertThat(member.defaultMember).isTrue()
        }

        @Test
        fun `3rd Member should have correct Concept, MemberCode and DefaultCode values`() {
            val member = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].members[2]

            member.concept.apply {
                assertThat(createdAt).isEqualTo(Instant.parse("2018-06-26T14:53:37.723Z"))
                assertThat(modifiedAt).isEqualTo(Instant.parse("2018-06-26T14:54:37.723Z"))

                assertThat(applicableFrom).isNull()
                assertThat(applicableUntil).isNull()

                assertThat(label.translations).containsOnly(
                    entry(en, "Comprehensive code 2 #en"),
                    entry(fi, "Comprehensive code 2 #fi")
                )

                assertThat(description).isNotNull()
                assertThat(description.translations).containsOnly(
                    entry(en, "Code 2 description #en"),
                    entry(fi, "Code 2 description #fi")
                )
            }

            assertThat(member.memberCode).isEqualTo("tf_cmpr_cl_code2")

            assertThat(member.defaultMember).isFalse()
        }
    }

    @Nested
    @DisplayName("and source has three DPM dictionaries")
    @Disabled
    inner class ThreeDpmDictionaries {

        @BeforeEach
        fun init() {
            yclSource = createYclSourceFromTestFixture("three_dpm_dictionaries")
        }

        @Test
        fun `Mapping result should have 3 DPM Dictionaries`() {
            val dpmDictionaries = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)
            assertThat(dpmDictionaries.size).isEqualTo(3)
        }

        @Test
        fun `1st DPM Dictionary should have correct Owner`() {
            val owner = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_0_Name")
        }

        @Test
        fun `2nd DPM Dictionary should have correct Owner`() {
            val owner = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[1].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_1_Name")
        }

        @Test
        fun `3rd DPM Dictionary should have correct Owner`() {
            val owner = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[2].owner
            assertThat(owner.name).isEqualTo("ThreeDpmDictionaries_2_Name")
        }
    }

    @Nested
    @DisplayName("and source has disordered codepages")
    @Disabled
    inner class ThreeYclCodePages {

        @BeforeEach
        fun init() {
            yclSource = createYclSourceFromTestFixture("disordered_ycl_codepages")
        }

        @Test
        fun `Dictionary should have correct Owner`() {
            val owner = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].owner
            assertThat(owner.name).isEqualTo("DisorderedYclCodepages_Name")
        }

        @Test
        fun `Explicit Domain should have 6 Members`() {
            val members = yclToDpmMapper.mapDpmDictionariesFromSource(yclSource)[0].explicitDomains[0].members

            assertThat(members.size).isEqualTo(6)
        }

        @Test
        fun `Members should be in correct order`() {
        }
    }

    private fun createYclSourceFromTestFixture(fixtureName: String): YclSource {
        val fixturePath = TestFixture.yclSourceCapturePath(fixtureName)
        return YclSourceFolderStructureAdapter(fixturePath)
    }
}
