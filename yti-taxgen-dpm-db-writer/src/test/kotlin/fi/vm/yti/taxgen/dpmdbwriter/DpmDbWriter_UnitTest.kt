package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.testcommons.DiagnosticConsumerCaptorSimple
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.columnConfigToString
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDate

@DisplayName("Dpm Database")
class DpmDbWriter_UnitTest {
    private lateinit var tempFolder: TempFolder

    private lateinit var diagnosticConsumerCaptor: DiagnosticConsumerCaptorSimple
    private lateinit var diagnostic: Diagnostic

    private lateinit var dbConnection: Connection

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("dpmdbwriter")

        val dbPath = tempFolder.resolve("dpm.db")

        diagnosticConsumerCaptor = DiagnosticConsumerCaptorSimple()
        diagnostic = DiagnosticBridge(diagnosticConsumerCaptor)

        val dbWriter = DpmDbWriter(
            dbPath,
            false,
            diagnostic
        )

        dbWriter.writeDpmDb(dpmDictionaryFixture())

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()
        dbConnection.close()
    }

    @Nested
    @DisplayName("table structure")
    inner class TableStructure {

        @Test
        fun `mLanguage table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mLanguage")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(5)

            assertThat(md.columnConfigToString(1)).isEqualTo("LanguageID, INTEGER, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("LanguageName, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("EnglishName, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("IsoCode, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(5)).isEqualTo("ConceptID, INT, Nullable, false")
        }

        @Test
        fun `mConcept table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mConcept")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(7)

            assertThat(md.columnConfigToString(1)).isEqualTo("ConceptID, INTEGER, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("ConceptType, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("OwnerID, INT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("CreationDate, DATE, Nullable, false")
            assertThat(md.columnConfigToString(5)).isEqualTo("ModificationDate, DATE, Nullable, false")
            assertThat(md.columnConfigToString(6)).isEqualTo("FromDate, DATE, Nullable, false")
            assertThat(md.columnConfigToString(7)).isEqualTo("ToDate, DATE, Nullable, false")
        }

        @Test
        fun `mConceptTranslation table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mConceptTranslation")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(4)

            assertThat(md.columnConfigToString(1)).isEqualTo("ConceptID, INT, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("LanguageID, INT, NonNullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("Text, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("Role, TEXT, NonNullable, false")
        }

        @Test
        fun `mOwner table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mOwner")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(8)
            assertThat(md.columnConfigToString(1)).isEqualTo("OwnerID, INTEGER, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("OwnerName, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("OwnerNamespace, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("OwnerLocation, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(5)).isEqualTo("OwnerPrefix, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(6)).isEqualTo("OwnerCopyright, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(7)).isEqualTo("ParentOwnerID, INT, Nullable, false")
            assertThat(md.columnConfigToString(8)).isEqualTo("ConceptID, INT, Nullable, false")
        }

        @Test
        fun `mDomain table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mDomain")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(8)
            assertThat(md.columnConfigToString(1)).isEqualTo("DomainID, INTEGER, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("DomainCode, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("DomainLabel, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("DomainDescription, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(5)).isEqualTo("DomainXBRLCode, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(6)).isEqualTo("DataType, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(7)).isEqualTo("IsTypedDomain, BOOLEAN, Nullable, false")
            assertThat(md.columnConfigToString(8)).isEqualTo("ConceptID, INT, Nullable, false")
        }

        @Test
        fun `mMember table should have correct columns`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM mMember")
            val md = rs.metaData

            assertThat(md.columnCount).isEqualTo(7)
            assertThat(md.columnConfigToString(1)).isEqualTo("MemberID, INTEGER, NonNullable, false")
            assertThat(md.columnConfigToString(2)).isEqualTo("MemberCode, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(3)).isEqualTo("MemberLabel, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(4)).isEqualTo("MemberXBRLCode, TEXT, Nullable, false")
            assertThat(md.columnConfigToString(5)).isEqualTo("IsDefaultMember, BOOLEAN, Nullable, false")
            assertThat(md.columnConfigToString(6)).isEqualTo("ConceptID, INT, Nullable, false")
            assertThat(md.columnConfigToString(7)).isEqualTo("DomainID, INT, Nullable, false")
        }
    }

    @Nested
    @DisplayName("data content")
    inner class DataContent {

        @Test
        fun `should have all configured languages`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT IsoCode FROM mLanguage")
            val dbIsoCodes = rs.toStringList()

            val allKnownIsoCodes = Language.languages().map { it.iso6391Code }.toList()
            assertThat(dbIsoCodes).containsExactlyInAnyOrderElementsOf(allKnownIsoCodes)

            assertThat(dbIsoCodes).size().isEqualTo(24)
        }

        @Test
        fun `should have English language with Concept and ConceptTranslation relations`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    L.IsoCode, L.LanguageName, L.EnglishName,
                    C.ConceptType, C.OwnerID, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mLanguage AS L
                INNER JOIN mConcept AS C ON C.ConceptID = L.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = L.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE L.IsoCode = 'en'
                """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "en, English, English, Language, nil, nil, nil, nil, nil, English, label, en",
                "en, English, English, Language, nil, nil, nil, nil, nil, englanti, label, fi",
                "en, English, English, Language, nil, nil, nil, nil, nil, engelska, label, sv"
            )
        }

        @Test
        fun `should have DPM Owner`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    O.OwnerName, O.OwnerNamespace, O.OwnerLocation, O.OwnerPrefix, O.OwnerCopyright, O.ParentOwnerID, O.ConceptID
                FROM mOwner AS O
                """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "OwnerName, OwnerNamespace, OwnerLocation, OwnerPrefix, OwnerCopyright, nil, nil"
            )
        }

        @Test
        fun `should have DPM ExplicitDomain with Concept and Owner relation`() {

            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    D.DomainCode, D.DomainLabel, D.DomainDescription, D.DomainXBRLCode, D.DataType, D.IsTypedDomain,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "DomainCode, ExplicitDomainLabelFi, ExplicitDomainDescriptionFi, OwnerPrefix_exp:DomainCode, nil, 0, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-01-01, 2018-12-15, OwnerName"
            )
        }

        @Test
        fun `should have ConceptTranslations for DPM ExplicitDomain`() {

            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    D.DomainCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mDomain AS D
                INNER JOIN mConcept AS C ON C.ConceptID = D.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE D.DomainCode = 'DomainCode'
              """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "DomainCode, Domain, ExplicitDomainLabelEn, label, en",
                "DomainCode, Domain, ExplicitDomainLabelFi, label, fi"
            )
        }

        @Test
        fun `should have DPM Member with Concept and Owner relation`() {

            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    M.MemberCode, M.MemberLabel, M.MemberXBRLCode, M.IsDefaultMember,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    D.DomainCode,
                    O.OwnerName
                FROM mMember AS M
                INNER JOIN mDomain AS D ON D.DomainID = D.DomainID
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "MemberCode, MemberLabelFi, OwnerPrefix_DomainCode:MemberCode, 1, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-02, nil, DomainCode, OwnerName"
            )
        }

        @Test
        fun `should have ConceptTranslations for DPM Member`() {

            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    M.MemberCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mMember AS M
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE M.MemberCode = 'MemberCode'
              """
            )

            val rows = rs.toStringList()

            assertThat(rows).containsExactlyInAnyOrder(
                "MemberCode, Member, MemberLabelEn, label, en",
                "MemberCode, Member, MemberLabelFi, label, fi"
            )
        }
    }

    private fun dpmDictionaryFixture(): List<DpmDictionary> {
        fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

        val dpmOwner = Owner(
            name = "OwnerName",
            namespace = "OwnerNamespace",
            prefix = "OwnerPrefix",
            location = "OwnerLocation",
            copyright = "OwnerCopyright",
            languages = setOf(language("en"), language("fi")),
            defaultLanguage = language("fi")
        )

        val dictionaries =
            listOf(
                DpmDictionary(
                    owner = dpmOwner,

                    explicitDomains = listOf(
                        ExplicitDomain(
                            concept = Concept(
                                createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
                                modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
                                applicableFrom = LocalDate.of(2018, 1, 1),
                                applicableUntil = LocalDate.of(2018, 12, 15),
                                label = TranslatedText(
                                    translations = listOf(
                                        Pair(language("fi"), "ExplicitDomainLabelFi"),
                                        Pair(language("en"), "ExplicitDomainLabelEn")
                                    ).toMap()
                                ),
                                description = TranslatedText(
                                    translations = listOf(
                                        Pair(language("fi"), "ExplicitDomainDescriptionFi"),
                                        Pair(language("en"), "ExplicitDomainDescriptionEn")
                                    ).toMap()
                                ),
                                owner = dpmOwner
                            ),
                            domainCode = "DomainCode",
                            members = listOf(
                                Member(
                                    concept = Concept(
                                        createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
                                        modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
                                        applicableFrom = LocalDate.of(2018, 2, 2),
                                        applicableUntil = null,
                                        label = TranslatedText(
                                            translations = listOf(
                                                Pair(language("fi"), "MemberLabelFi"),
                                                Pair(language("en"), "MemberLabelEn")
                                            ).toMap()
                                        ),
                                        description = TranslatedText(
                                            translations = listOf(
                                                Pair(language("fi"), "MemberDescriptionFi"),
                                                Pair(language("en"), "MemberDescriptionEn")
                                            ).toMap()
                                        ),
                                        owner = dpmOwner
                                    ),
                                    memberCode = "MemberCode",
                                    defaultMember = true
                                )
                            )
                        )
                    )
                )
            )
        return dictionaries
    }

    @Nested
    @DisplayName("diagnostic events")
    inner class DiagnosticEvents {

        @Test
        fun `should contain proper context events`() {
            assertThat(diagnosticConsumerCaptor.events).containsExactly(
                "ENTER [ActivityWriteDpmDb]",
                "EXIT [] RETIRED [ActivityWriteDpmDb]"
            )
        }
    }
}
