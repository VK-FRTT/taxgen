package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.testcommons.DiagnosticConsumerCaptor
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSetMetaData
import java.sql.ResultSetMetaData.columnNoNulls
import java.sql.ResultSetMetaData.columnNullable
import java.time.Instant
import java.time.LocalDate

@DisplayName("Dpm Database writer")
class DpmDbWriter_UnitTest {
    private lateinit var tempFolder: TempFolder

    private lateinit var dbConnection: Connection

    @BeforeEach
    fun init() {
        tempFolder = TempFolder("dpmdbwriter")

        val dbPath = tempFolder.resolve("dpm.db")

        val diagnosticConsumerCaptor = DiagnosticConsumerCaptor()
        val diagnostic = DiagnosticBridge(diagnosticConsumerCaptor)

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

    @Test
    fun `Should configure mLanguage table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mLanguage")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(5)
        md.assertColumnConfig(1, "LanguageID", "INTEGER", columnNoNulls, false)
        md.assertColumnConfig(2, "LanguageName", "TEXT", columnNullable, false)
        md.assertColumnConfig(3, "EnglishName", "TEXT", columnNullable, false)
        md.assertColumnConfig(4, "IsoCode", "TEXT", columnNullable, false)
        md.assertColumnConfig(5, "ConceptID", "INT", columnNullable, false)
    }

    @Test
    fun `Should configure mConcept table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mConcept")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(7)
        md.assertColumnConfig(1, "ConceptID", "INTEGER", columnNoNulls, false)
        md.assertColumnConfig(2, "ConceptType", "TEXT", columnNullable, false)
        md.assertColumnConfig(3, "OwnerID", "INT", columnNullable, false)
        md.assertColumnConfig(4, "CreationDate", "DATE", columnNullable, false)
        md.assertColumnConfig(5, "ModificationDate", "DATE", columnNullable, false)
        md.assertColumnConfig(6, "FromDate", "DATE", columnNullable, false)
        md.assertColumnConfig(7, "ToDate", "DATE", columnNullable, false)
    }

    @Test
    fun `Should configure mConceptTranslation table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mConceptTranslation")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(4)
        md.assertColumnConfig(1, "ConceptID", "INT", columnNoNulls, false)
        md.assertColumnConfig(2, "LanguageID", "INT", columnNoNulls, false)
        md.assertColumnConfig(3, "Text", "TEXT", columnNullable, false)
        md.assertColumnConfig(4, "Role", "TEXT", columnNoNulls, false)
    }

    @Test
    fun `Should configure mOwner table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mOwner")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(8)
        md.assertColumnConfig(1, "OwnerID", "INTEGER", columnNoNulls, false)
        md.assertColumnConfig(2, "OwnerName", "TEXT", columnNullable, false)
        md.assertColumnConfig(3, "OwnerNamespace", "TEXT", columnNullable, false)
        md.assertColumnConfig(4, "OwnerLocation", "TEXT", columnNullable, false)
        md.assertColumnConfig(5, "OwnerPrefix", "TEXT", columnNullable, false)
        md.assertColumnConfig(6, "OwnerCopyright", "TEXT", columnNullable, false)
        md.assertColumnConfig(7, "ParentOwnerID", "INT", columnNullable, false)
        md.assertColumnConfig(8, "ConceptID", "INT", columnNullable, false)
    }

    @Test
    fun `Should configure mDomain table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mDomain")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(8)
        md.assertColumnConfig(1, "DomainID", "INTEGER", columnNoNulls, false)
        md.assertColumnConfig(2, "DomainCode", "TEXT", columnNullable, false)
        md.assertColumnConfig(3, "DomainLabel", "TEXT", columnNullable, false)
        md.assertColumnConfig(4, "DomainDescription", "TEXT", columnNullable, false)
        md.assertColumnConfig(5, "DomainXBRLCode", "TEXT", columnNullable, false)
        md.assertColumnConfig(6, "DataType", "TEXT", columnNullable, false)
        md.assertColumnConfig(7, "IsTypedDomain", "BOOLEAN", columnNullable, false)
        md.assertColumnConfig(8, "ConceptID", "INT", columnNullable, false)
    }

    @Test
    fun `Should configure mMember table with correct columns`() {
        val rs = dbConnection.createStatement().executeQuery("select * from mMember")
        val md = rs.metaData

        assertThat(md.columnCount).isEqualTo(7)
        md.assertColumnConfig(1, "MemberID", "INTEGER", columnNoNulls, false)
        md.assertColumnConfig(2, "MemberCode", "TEXT", columnNullable, false)
        md.assertColumnConfig(3, "MemberLabel", "TEXT", columnNullable, false)
        md.assertColumnConfig(4, "MemberXBRLCode", "TEXT", columnNullable, false)
        md.assertColumnConfig(5, "IsDefaultMember", "BOOLEAN", columnNullable, false)
        md.assertColumnConfig(6, "ConceptID", "INT", columnNullable, false)
        md.assertColumnConfig(7, "DomainID", "INT", columnNullable, false)
    }

    private fun ResultSetMetaData.assertColumnConfig(
        column: Int,
        expectedName: String,
        expectedTypeName: String,
        expectedNullable: Int,
        expectedAutoIncrement: Boolean
    ) {
        assertThat(getColumnName(column)).isEqualTo(expectedName)
        assertThat(getColumnTypeName(column)).isEqualTo(expectedTypeName)
        assertThat(isNullable(column)).isEqualTo(expectedNullable)
        assertThat(isAutoIncrement(column)).isEqualTo(expectedAutoIncrement)
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
                                applicableFrom = LocalDate.of(2018, 3, 20),
                                applicableUntil = LocalDate.of(2018, 3, 20),
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
                            domainCode = "DomainCodeValue",
                            members = listOf(
                                Member(
                                    concept = Concept(
                                        createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
                                        modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
                                        applicableFrom = LocalDate.of(2018, 3, 20),
                                        applicableUntil = LocalDate.of(2018, 3, 20),
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
                                    memberCode = "MemberCodeValue",
                                    defaultMember = true
                                )
                            )
                        )
                    )
                )
            )
        return dictionaries
    }
}
