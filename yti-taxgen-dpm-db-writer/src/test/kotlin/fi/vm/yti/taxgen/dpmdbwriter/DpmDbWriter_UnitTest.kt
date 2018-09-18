package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.datapointmetamodel.Concept
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.datapointmetamodel.ExplicitDomain
import fi.vm.yti.taxgen.datapointmetamodel.Hierarchy
import fi.vm.yti.taxgen.datapointmetamodel.HierarchyNode
import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Member
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.datapointmetamodel.TranslatedText
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.DomainTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.HierarchyTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.LanguageTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.MemberTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.OwnerTable
import fi.vm.yti.taxgen.testcommons.DiagnosticConsumerCaptorSimple
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.columnConfigToString
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Table
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DatabaseMetaData.importedKeyCascade
import java.sql.DatabaseMetaData.importedKeyInitiallyDeferred
import java.sql.DatabaseMetaData.importedKeyInitiallyImmediate
import java.sql.DatabaseMetaData.importedKeyNoAction
import java.sql.DatabaseMetaData.importedKeyNotDeferrable
import java.sql.DatabaseMetaData.importedKeyRestrict
import java.sql.DatabaseMetaData.importedKeySetDefault
import java.sql.DatabaseMetaData.importedKeySetNull
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.ResultSetMetaData
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
        private var verifiedTable: Table? = null

        @AfterEach
        fun baseTeardown() {
            verifiedTable = null
        }

        @Test
        fun `mLanguage table should have correct structure`() {
            verifiedTable = LanguageTable
            assertThat(verifiedTableName()).isEqualTo("mLanguage")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(5)

            assertThat(meta.columnConfigToString(1)).isEqualTo("LanguageID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("LanguageName, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("EnglishName, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("IsoCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("ConceptID, INT, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "LanguageID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mConcept table should have correct structure`() {
            verifiedTable = ConceptTable
            assertThat(verifiedTableName()).isEqualTo("mConcept")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(7)

            assertThat(meta.columnConfigToString(1)).isEqualTo("ConceptID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("ConceptType, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("OwnerID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("CreationDate, DATE, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("ModificationDate, DATE, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("FromDate, DATE, Nullable, false")
            assertThat(meta.columnConfigToString(7)).isEqualTo("ToDate, DATE, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "OwnerID, mOwner, OwnerID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mConceptTranslation table should have correct structure`() {
            verifiedTable = ConceptTranslationTable
            assertThat(verifiedTableName()).isEqualTo("mConceptTranslation")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(4)

            assertThat(meta.columnConfigToString(1)).isEqualTo("ConceptID, INT, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("LanguageID, INT, NonNullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("Text, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("Role, TEXT, NonNullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, 1",
                "LanguageID, 2",
                "Role, 3"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "LanguageID, mLanguage, LanguageID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mOwner table should have correct structure`() {
            verifiedTable = OwnerTable
            assertThat(verifiedTableName()).isEqualTo("mOwner")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(8)
            assertThat(meta.columnConfigToString(1)).isEqualTo("OwnerID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("OwnerName, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("OwnerNamespace, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("OwnerLocation, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("OwnerPrefix, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("OwnerCopyright, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(7)).isEqualTo("ParentOwnerID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(8)).isEqualTo("ConceptID, INT, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "OwnerID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ParentOwnerID, mOwner, OwnerID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mDomain table should have correct structure`() {
            verifiedTable = DomainTable
            assertThat(verifiedTableName()).isEqualTo("mDomain")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(8)
            assertThat(meta.columnConfigToString(1)).isEqualTo("DomainID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("DomainCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("DomainLabel, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("DomainDescription, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("DomainXBRLCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("DataType, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(7)).isEqualTo("IsTypedDomain, BOOLEAN, Nullable, false")
            assertThat(meta.columnConfigToString(8)).isEqualTo("ConceptID, INT, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "DomainID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mMember table should have correct structure`() {
            verifiedTable = MemberTable
            assertThat(verifiedTableName()).isEqualTo("mMember")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(7)
            assertThat(meta.columnConfigToString(1)).isEqualTo("MemberID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("MemberCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("MemberLabel, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("MemberXBRLCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("IsDefaultMember, BOOLEAN, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("ConceptID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(7)).isEqualTo("DomainID, INT, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "MemberID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "DomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mHierarchy table should have correct structure`() {
            verifiedTable = HierarchyTable
            assertThat(verifiedTableName()).isEqualTo("mHierarchy")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(6)
            assertThat(meta.columnConfigToString(1)).isEqualTo("HierarchyID, INTEGER, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("HierarchyCode, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("HierarchyLabel, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("DomainID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("HierarchyDescription, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("ConceptID, INT, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "HierarchyID, 1"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "DomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        @Test
        fun `mHierarchyNode table should have correct structure`() {
            verifiedTable = HierarchyNodeTable
            assertThat(verifiedTableName()).isEqualTo("mHierarchyNode")

            val meta = resultSetMetaDataOfVerifiedTable()

            assertThat(meta.columnCount).isEqualTo(11)
            assertThat(meta.columnConfigToString(1)).isEqualTo("HierarchyID, INT, NonNullable, false")
            assertThat(meta.columnConfigToString(2)).isEqualTo("MemberID, INT, NonNullable, false")
            assertThat(meta.columnConfigToString(3)).isEqualTo("IsAbstract, BOOLEAN, Nullable, false")
            assertThat(meta.columnConfigToString(4)).isEqualTo("ComparisonOperator, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(5)).isEqualTo("UnaryOperator, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(6)).isEqualTo("Order, INT, Nullable, false")
            assertThat(meta.columnConfigToString(7)).isEqualTo("Level, INT, Nullable, false")
            assertThat(meta.columnConfigToString(8)).isEqualTo("ParentMemberID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(9)).isEqualTo("HierarchyNodeLabel, TEXT, Nullable, false")
            assertThat(meta.columnConfigToString(10)).isEqualTo("ConceptID, INT, Nullable, false")
            assertThat(meta.columnConfigToString(11)).isEqualTo("Path, VARCHAR, Nullable, false")

            assertThat(primaryKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "HierarchyID, 1",
                "MemberID, 2"
            )

            assertThat(foreignKeysOfVerifiedTable()).containsExactlyInAnyOrder(
                "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "HierarchyID, mHierarchy, HierarchyID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                "MemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
            )
        }

        private fun verifiedTableName() = verifiedTable!!.tableName

        private fun resultSetMetaDataOfVerifiedTable(): ResultSetMetaData {
            val rs = dbConnection.createStatement().executeQuery("SELECT * FROM ${verifiedTableName()}")
            return rs.metaData
        }

        private fun primaryKeysOfVerifiedTable(): List<String> {
            val separator = ", "
            val rows = mutableListOf<String>()

            val pKeys = dbConnection.metaData.getPrimaryKeys(null, null, verifiedTableName())
            while (pKeys.next()) {

                val sb = StringBuilder()

                sb.append(pKeys.getString("COLUMN_NAME"))
                sb.append(separator)
                sb.append(pKeys.getInt("KEY_SEQ"))

                rows.add(sb.toString())
            }
            return rows
        }

        private fun foreignKeysOfVerifiedTable(): List<String> {

            fun importedKeysConstantColumn(resultSet: ResultSet, columnName: String): String {
                val valueSymbol = when (resultSet.getInt(columnName)) {
                    importedKeyCascade -> "Cascade"
                    importedKeyRestrict -> "Restrict"
                    importedKeySetNull -> "SetNull"
                    importedKeyNoAction -> "NoAction"
                    importedKeySetDefault -> "SetDefault"
                    importedKeyInitiallyDeferred -> "InitiallyDeferred"
                    importedKeyInitiallyImmediate -> "InitiallyImmediate"
                    importedKeyNotDeferrable -> "NotDeferrable"
                    else -> "unknown"
                }

                return "$columnName:$valueSymbol"
            }

            val separator = ", "
            val rows = mutableListOf<String>()

            val pKeys = dbConnection.metaData.getImportedKeys(null, null, verifiedTableName())
            while (pKeys.next()) {

                val sb = StringBuilder()

                sb.append(pKeys.getString("FKCOLUMN_NAME"))
                sb.append(separator)
                sb.append(pKeys.getString("PKTABLE_NAME"))
                sb.append(separator)
                sb.append(pKeys.getString("PKCOLUMN_NAME"))

                sb.append(separator)
                sb.append(pKeys.getInt("KEY_SEQ"))

                sb.append(separator)
                sb.append(importedKeysConstantColumn(pKeys, "UPDATE_RULE"))
                sb.append(separator)
                sb.append(importedKeysConstantColumn(pKeys, "DELETE_RULE"))
                sb.append(separator)
                sb.append(importedKeysConstantColumn(pKeys, "DEFERRABILITY"))

                rows.add(sb.toString())
            }
            return rows
        }
    }

    @Nested
    @DisplayName("data content")
    inner class DataContent {

        @Test
        fun `should have all configured languages`() {
            val rs = dbConnection.createStatement().executeQuery("SELECT IsoCode FROM mLanguage")
            val dbIsoCodes = rs.toStringList(false)

            val allKnownIsoCodes = Language.languages().map { it.iso6391Code }.toList()
            assertThat(dbIsoCodes).containsExactlyInAnyOrderElementsOf(allKnownIsoCodes)

            assertThat(dbIsoCodes).size().isEqualTo(24)
        }

        @Test
        fun `should have English language with Concept and ConceptTranslation relations`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    L.IsoCode AS LanguageIsoCode, L.LanguageName, L.EnglishName AS LanguageEnglishName,
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

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "LanguageIsoCode, LanguageName, LanguageEnglishName, ConceptType, OwnerID, CreationDate, ModificationDate, FromDate, ToDate, Text, Role, IsoCode",
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

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "OwnerName, OwnerNamespace, OwnerLocation, OwnerPrefix, OwnerCopyright, ParentOwnerID, ConceptID",
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

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "DomainCode, DomainLabel, DomainDescription, DomainXBRLCode, DataType, IsTypedDomain, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
                "Domain-Code, ExplicitDomain-LabelFi, ExplicitDomain-DescriptionFi, OwnerPrefix_exp:Domain-Code, nil, 0, Domain, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName"
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
                WHERE D.DomainCode = 'Domain-Code'
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "DomainCode, ConceptType, Text, Role, IsoCode",
                "Domain-Code, Domain, ExplicitDomain-LabelEn, label, en",
                "Domain-Code, Domain, ExplicitDomain-LabelFi, label, fi"
            )
        }

        @Test
        fun `should have DPM Members with Domain, Concept and Owner relation`() {

            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    M.MemberCode, M.MemberLabel, M.MemberXBRLCode, M.IsDefaultMember,
                    D.DomainCode,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mMember AS M
                INNER JOIN mDomain AS D ON D.DomainID = M.DomainID
                INNER JOIN mConcept AS C ON C.ConceptID = M.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "MemberCode, MemberLabel, MemberXBRLCode, IsDefaultMember, DomainCode, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
                "Member-1-Code, Member-1-LabelFi, OwnerPrefix_Domain-Code:Member-1-Code, 1, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "Member-2-Code, Member-2-LabelFi, OwnerPrefix_Domain-Code:Member-2-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "Member-3-Code, Member-3-LabelFi, OwnerPrefix_Domain-Code:Member-3-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "Member-4-Code, Member-4-LabelFi, OwnerPrefix_Domain-Code:Member-4-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "Member-5-Code, Member-5-LabelFi, OwnerPrefix_Domain-Code:Member-5-Code, 0, Domain-Code, Member, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName"
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
                WHERE M.MemberCode = 'Member-1-Code'
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "MemberCode, ConceptType, Text, Role, IsoCode",
                "Member-1-Code, Member, Member-1-LabelEn, label, en",
                "Member-1-Code, Member, Member-1-LabelFi, label, fi"
            )
        }

        @Test
        fun `should have DPM Hierarchy with Domain, Concept and Owner relation`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    H.HierarchyCode, H.HierarchyLabel, H.HierarchyDescription,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    D.DomainCode,
                    O.OwnerName
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mDomain AS D ON D.DomainID = H.DomainID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "HierarchyCode, HierarchyLabel, HierarchyDescription, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, DomainCode, OwnerName",
                "Hierarchy-Code, Hierarchy-LabelFi, Hierarchy-DescriptionFi, Hierarchy, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, Domain-Code, OwnerName"
            )
        }

        @Test
        fun `should have ConceptTranslations for DPM Hierarchy`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    H.HierarchyCode,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mHierarchy AS H
                INNER JOIN mConcept AS C ON C.ConceptID = H.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE H.HierarchyCode = 'Hierarchy-Code'
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "HierarchyCode, ConceptType, Text, Role, IsoCode",
                "Hierarchy-Code, Hierarchy, Hierarchy-LabelEn, label, en",
                "Hierarchy-Code, Hierarchy, Hierarchy-LabelFi, label, fi"
            )
        }

        @Test
        fun `should have DPM HierarchyNodes with Member, Concept and Owner relation`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
					N.HierarchyNodeLabel, N.ComparisonOperator, N.UnaryOperator, N.IsAbstract, N.'Order', N.Level, N.Path,
					H.HierarchyCode,
					M.MemberCode,
					P.MemberCode AS ParentMemberCode,
                    C.ConceptType, C.CreationDate, C.ModificationDate, C.FromDate, C.ToDate,
                    O.OwnerName
                FROM mHierarchyNode AS N
                INNER JOIN mHierarchy AS H ON H.HierarchyID = N.HierarchyID
				INNER JOIN mMember AS M ON M.MemberID = N.MemberID
				LEFT JOIN mMember AS P ON P.MemberID = N.ParentMemberID
                INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                INNER JOIN mOwner AS O ON C.OwnerID = O.OwnerID
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "HierarchyNodeLabel, ComparisonOperator, UnaryOperator, IsAbstract, Order, Level, Path, HierarchyCode, MemberCode, ParentMemberCode, ConceptType, CreationDate, ModificationDate, FromDate, ToDate, OwnerName",
                "HierarchyNode-1-LabelFi, nil, nil, 0, 1, 1, nil, Hierarchy-Code, Member-1-Code, nil, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "HierarchyNode-2-LabelFi, =, +, 0, 2, 1, nil, Hierarchy-Code, Member-2-Code, nil, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "HierarchyNode-2.1-LabelFi, =, +, 0, 1, 2, nil, Hierarchy-Code, Member-3-Code, Member-2-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "HierarchyNode-2.1.1-LabelFi, nil, nil, 0, 1, 3, nil, Hierarchy-Code, Member-4-Code, Member-3-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName",
                "HierarchyNode-2.2-LabelFi, nil, nil, 0, 2, 2, nil, Hierarchy-Code, Member-5-Code, Member-2-Code, HierarchyNode, 2018-09-03 10:12:25Z, 2018-09-03 22:10:36Z, 2018-02-22, 2018-05-15, OwnerName"
            )
        }

        @Test
        fun `should have ConceptTranslations for DPM HierarchyNode`() {
            val rs = dbConnection.createStatement().executeQuery(
                """
                SELECT
                    N.HierarchyNodeLabel,
                    C.ConceptType,
                    T.Text, T.Role,
                    TL.IsoCode
                FROM mHierarchyNode AS N
                INNER JOIN mConcept AS C ON C.ConceptID = N.ConceptID
                INNER JOIN mConceptTranslation AS T ON T.ConceptID = C.ConceptID
                INNER JOIN mLanguage AS TL ON T.LanguageID = TL.LanguageID
                WHERE N.'Order'= 1 AND N.Level = 1
              """
            )

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "HierarchyNodeLabel, ConceptType, Text, Role, IsoCode",
                "HierarchyNode-1-LabelFi, HierarchyNode, HierarchyNode-1-LabelEn, label, en",
                "HierarchyNode-1-LabelFi, HierarchyNode, HierarchyNode-1-LabelFi, label, fi"
            )
        }
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

        fun concept(name: String) = Concept(
            createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
            modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
            applicableFrom = LocalDate.of(2018, 2, 22),
            applicableUntil = LocalDate.of(2018, 5, 15),
            label = TranslatedText(
                translations = listOf(
                    Pair(language("fi"), "$name-LabelFi"),
                    Pair(language("en"), "$name-LabelEn")
                ).toMap()
            ),
            description = TranslatedText(
                translations = listOf(
                    Pair(language("fi"), "$name-DescriptionFi"),
                    Pair(language("en"), "$name-DescriptionEn")
                ).toMap()
            ),
            owner = dpmOwner
        )

        val members = listOf(
            Member(
                concept = concept("Member-1"),
                memberCode = "Member-1-Code",
                defaultMember = true
            ),
            Member(
                concept = concept("Member-2"),
                memberCode = "Member-2-Code",
                defaultMember = false
            ),
            Member(
                concept = concept("Member-3"),
                memberCode = "Member-3-Code",
                defaultMember = false
            ),
            Member(
                concept = concept("Member-4"),
                memberCode = "Member-4-Code",
                defaultMember = false
            ),
            Member(
                concept = concept("Member-5"),
                memberCode = "Member-5-Code",
                defaultMember = false
            )
        )

        val hierarchyNodes = listOf(
            HierarchyNode(
                concept = concept("HierarchyNode-1"),
                abstract = false,
                comparisonOperator = null,
                unaryOperator = null,
                member = members[0],
                childNodes = null
            ),

            HierarchyNode(
                concept = concept("HierarchyNode-2"),
                abstract = false,
                comparisonOperator = "=",
                unaryOperator = "+",
                member = members[1],
                childNodes = listOf(
                    HierarchyNode(
                        concept = concept("HierarchyNode-2.1"),
                        abstract = false,
                        comparisonOperator = "=",
                        unaryOperator = "+",
                        member = members[2],
                        childNodes = listOf(
                            HierarchyNode(
                                concept = concept("HierarchyNode-2.1.1"),
                                abstract = false,
                                comparisonOperator = null,
                                unaryOperator = null,
                                member = members[3],
                                childNodes = null
                            )
                        )
                    ),

                    HierarchyNode(
                        concept = concept("HierarchyNode-2.2"),
                        abstract = false,
                        comparisonOperator = null,
                        unaryOperator = null,
                        member = members[4],
                        childNodes = null
                    )
                )
            )
        )

        val hierarchies = listOf(
            Hierarchy(
                concept = concept("Hierarchy"),
                hierarchyCode = "Hierarchy-Code",
                rootNodes = hierarchyNodes
            )
        )

        val dictionaries =
            listOf(
                DpmDictionary(
                    owner = dpmOwner,

                    explicitDomains = listOf(
                        ExplicitDomain(
                            concept = concept("ExplicitDomain"),
                            domainCode = "Domain-Code",
                            members = members,
                            hierarchies = hierarchies
                        )
                    )
                )
            )

        return dictionaries
    }
}
