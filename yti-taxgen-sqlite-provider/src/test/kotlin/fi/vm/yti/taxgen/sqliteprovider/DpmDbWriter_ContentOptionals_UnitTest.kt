package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.DpmModelOptions
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("SQLite DPM DB content: configurable features")
internal class DpmDbWriter_ContentOptionals_UnitTest : SQLiteProvider_ContentUnitTestBase() {

    val domainLabelTranslationsQuery =
        """
        SELECT
            D.DomainCode,
            C.ConceptType,
            T.Role,
            TL.IsoCode,
            T.Text
        FROM mDomain AS D
        INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
        INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
        INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
        WHERE D.DomainCode = 'ExpDom-1-Code'
        ORDER BY D.DomainCode, T.Role DESC, TL.IsoCode
        """

    @Nested
    inner class MandatoryLabelTranslationOption {

        @Test
        fun `should support configurable Mandatory Language and Source Candidate Languages (FI injected to EN)`() {
            setupDbViaDictionaryCreate(
                false,
                FixtureVariety.TRANSLATIONS_FI_ONLY,
                mapOf(
                    DpmModelOptions.SqliteDb_MandatoryLabelTranslationLanguage to
                        Language.byIso6391CodeOrFail("en"),

                    DpmModelOptions.SqliteDb_MandatoryLabelTranslation_SourceCandidateLanguages to
                        listOf(
                            Language.byIso6391CodeOrFail("fi"),
                            Language.byIso6391CodeOrFail("sv")
                        )
                )
            )

            val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-Fi",
                "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
            )
        }

        @Test
        fun `should treat Source Candidate Languages as prioritized list (SV injected to EN)`() {
            setupDbViaDictionaryCreate(
                false,
                FixtureVariety.TRANSLATIONS_FI_SV,
                mapOf(
                    DpmModelOptions.SqliteDb_MandatoryLabelTranslationLanguage to
                        Language.byIso6391CodeOrFail("en"),

                    DpmModelOptions.SqliteDb_MandatoryLabelTranslation_SourceCandidateLanguages to
                        listOf(
                            Language.byIso6391CodeOrFail("fr"),
                            Language.byIso6391CodeOrFail("sv"),
                            Language.byIso6391CodeOrFail("fi")
                        )
                )
            )

            val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-Sv",
                "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                "ExpDom-1-Code, Domain, label, sv, ExpDom-Lbl-Sv",
                "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi",
                "ExpDom-1-Code, Domain, description, sv, ExpDom-Desc-Sv"
            )
        }

        @Test
        fun `should not produce Mandatory Language translation when no suitable Source Candidate Languages is found`() {
            setupDbViaDictionaryCreate(
                false,
                FixtureVariety.TRANSLATIONS_FI_ONLY,
                mapOf(
                    DpmModelOptions.SqliteDb_MandatoryLabelTranslationLanguage to
                        Language.byIso6391CodeOrFail("en"),

                    DpmModelOptions.SqliteDb_MandatoryLabelTranslation_SourceCandidateLanguages to
                        listOf(
                            Language.byIso6391CodeOrFail("fr"),
                            Language.byIso6391CodeOrFail("sv")
                        )
                )
            )

            val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
            )
        }
    }

    @Nested
    inner class DpmElementUriStorageOption {

        @Test
        fun `should support configurable URI Storage Language (URI stored as PL)`() {
            setupDbViaDictionaryCreate(
                false,
                FixtureVariety.NONE,
                mapOf(
                    DpmModelOptions.SqliteDb_DpmElementUriStorage_LabelTranslationLanguage to
                        Language.byIso6391CodeOrFail("pl")
                )
            )

            val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-En",
                "ExpDom-1-Code, Domain, label, fi, ExpDom-Lbl-Fi",
                "ExpDom-1-Code, Domain, label, pl, ExpDom-1-Uri",
                "ExpDom-1-Code, Domain, description, en, ExpDom-Desc-En",
                "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
            )
        }

        @Test
        fun `should produce diagnostic event when target language already has a translation (URI stored as FI)`() {
            setupDbViaDictionaryCreate(
                false,
                FixtureVariety.NONE,
                mapOf(
                    DpmModelOptions.SqliteDb_DpmElementUriStorage_LabelTranslationLanguage to
                        Language.byIso6391CodeOrFail("fi")
                )
            )

            val rs = dbConnection.createStatement().executeQuery(domainLabelTranslationsQuery)

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#DomainCode, #ConceptType, #Role, #IsoCode, #Text",
                "ExpDom-1-Code, Domain, label, en, ExpDom-Lbl-En",
                "ExpDom-1-Code, Domain, label, fi, ExpDom-1-Uri",
                "ExpDom-1-Code, Domain, description, en, ExpDom-Desc-En",
                "ExpDom-1-Code, Domain, description, fi, ExpDom-Desc-Fi"
            )

            assertThat(diagnosticCollector.eventsString()).contains(
                "MESSAGE [INFO] [DPM Element URI overwrites existing translation: ExpDom-Lbl-Fi (fi)]"
            )
        }
    }
}
