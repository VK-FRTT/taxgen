package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.LanguageTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.OwnerTable
import fi.vm.yti.taxgen.testcommons.ext.java.columnConfigToString
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Table
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.sql.DatabaseMetaData.importedKeyCascade
import java.sql.DatabaseMetaData.importedKeyInitiallyDeferred
import java.sql.DatabaseMetaData.importedKeyInitiallyImmediate
import java.sql.DatabaseMetaData.importedKeyNoAction
import java.sql.DatabaseMetaData.importedKeyNotDeferrable
import java.sql.DatabaseMetaData.importedKeyRestrict
import java.sql.DatabaseMetaData.importedKeySetDefault
import java.sql.DatabaseMetaData.importedKeySetNull
import java.sql.ResultSet
import java.sql.ResultSetMetaData

@DisplayName("SQLite DPM Database - table structure")
internal class DpmDbWriter_Structure_UnitTest : DpmDbWriter_UnitTestBase() {
    private var verifiedTable: Table? = null

    @BeforeEach
    fun init() {
        dbWriter.writeDpmDb(dpmDictionaryFixture())
    }

    @AfterEach
    fun teardown() {
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
