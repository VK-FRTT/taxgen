package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteprovider.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteprovider.tables.LanguageTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MetricTable
import fi.vm.yti.taxgen.sqliteprovider.tables.OwnerTable
import fi.vm.yti.taxgen.sqliteprovider.tables.Tables
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.columnConfigToString
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.nio.file.Files
import java.nio.file.StandardCopyOption
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

@DisplayName("SQLite DPM Database - table structure")
internal class DpmDbWriter_Structure_UnitTest {

    private lateinit var tempFolder: TempFolder
    private lateinit var seedBasedDbConnection: Connection
    private lateinit var selfCreatedDbConnection: Connection

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider")
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()

        if (::seedBasedDbConnection.isInitialized) {
            seedBasedDbConnection.close()
        }

        if (::selfCreatedDbConnection.isInitialized) {
            selfCreatedDbConnection.close()
        }
    }

    @TestFactory
    fun `Verify structure with seed based database`(): List<DynamicNode> {
        val dbPath = tempFolder.resolve("seed_based_dpm.db")

        val stream = this::class.java.getResourceAsStream("/dm_database_seed.db")
        Files.copy(stream, dbPath, StandardCopyOption.REPLACE_EXISTING)

        seedBasedDbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        return createTableStructureTests(seedBasedDbConnection)
    }

    @TestFactory
    fun `Verify structure with self initialized database`(): List<DynamicNode> {
        val dbPath = tempFolder.resolve("self_created_dpm.db")

        Database.connect("jdbc:sqlite:$dbPath", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        Tables.create()

        selfCreatedDbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        return createTableStructureTests(selfCreatedDbConnection)
    }

    private fun createTableStructureTests(dbConnection: Connection): List<DynamicNode> {

        return listOf(
            DynamicTest.dynamicTest("mLanguage") {
                val table = LanguageTable
                assertThat(table.tableName).isEqualTo("mLanguage")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(5)

                assertThat(meta.columnConfigToString(1)).isEqualTo("LanguageID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("LanguageName, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("EnglishName, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("IsoCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "LanguageID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mConcept") {
                val table = ConceptTable
                assertThat(table.tableName).isEqualTo("mConcept")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(7)

                assertThat(meta.columnConfigToString(1)).isEqualTo("ConceptID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("ConceptType, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("OwnerID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("CreationDate, DATE, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("ModificationDate, DATE, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("FromDate, DATE, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("ToDate, DATE, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "OwnerID, mOwner, OwnerID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mConceptTranslation") {
                val table = ConceptTranslationTable
                assertThat(table.tableName).isEqualTo("mConceptTranslation")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(4)

                assertThat(meta.columnConfigToString(1)).isEqualTo("ConceptID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("LanguageID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("Text, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("Role, VARCHAR, NonNullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, 1",
                    "LanguageID, 2",
                    "Role, 3"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "LanguageID, mLanguage, LanguageID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mOwner") {
                val table = OwnerTable
                assertThat(table.tableName).isEqualTo("mOwner")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(8)
                assertThat(meta.columnConfigToString(1)).isEqualTo("OwnerID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("OwnerName, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("OwnerNamespace, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("OwnerLocation, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("OwnerPrefix, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("OwnerCopyright, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("ParentOwnerID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(8)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "OwnerID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ParentOwnerID, mOwner, OwnerID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mDomain") {
                val table = DomainTable
                assertThat(table.tableName).isEqualTo("mDomain")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(8)
                assertThat(meta.columnConfigToString(1)).isEqualTo("DomainID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("DomainCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("DomainLabel, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("DomainDescription, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("DomainXBRLCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("DataType, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("IsTypedDomain, BOOLEAN, Nullable, false")
                assertThat(meta.columnConfigToString(8)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "DomainID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mMember") {
                val table = MemberTable
                assertThat(table.tableName).isEqualTo("mMember")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(7)
                assertThat(meta.columnConfigToString(1)).isEqualTo("MemberID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("DomainID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("MemberCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("MemberLabel, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("MemberXBRLCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("IsDefaultMember, BOOLEAN, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "MemberID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "DomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mHierarchy") {
                val table = HierarchyTable
                assertThat(table.tableName).isEqualTo("mHierarchy")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(6)
                assertThat(meta.columnConfigToString(1)).isEqualTo("HierarchyID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("HierarchyCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("HierarchyLabel, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("DomainID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("HierarchyDescription, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "HierarchyID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "DomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mHierarchyNode") {
                val table = HierarchyNodeTable
                assertThat(table.tableName).isEqualTo("mHierarchyNode")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(11)
                assertThat(meta.columnConfigToString(1)).isEqualTo("HierarchyID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("MemberID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("IsAbstract, BOOLEAN, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("ComparisonOperator, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("UnaryOperator, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("Order, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("Level, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(8)).isEqualTo("ParentMemberID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(9)).isEqualTo("HierarchyNodeLabel, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(10)).isEqualTo("ConceptID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(11)).isEqualTo("Path, VARCHAR, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "HierarchyID, 1",
                    "MemberID, 2"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "HierarchyID, mHierarchy, HierarchyID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "MemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mDimension") {
                val table = DimensionTable
                assertThat(table.tableName).isEqualTo("mDimension")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(8)
                assertThat(meta.columnConfigToString(1)).isEqualTo("DimensionID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("DimensionLabel, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("DimensionCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("DimensionDescription, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("DimensionXBRLCode, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("DomainID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("IsTypedDimension, BOOLEAN, Nullable, false")
                assertThat(meta.columnConfigToString(8)).isEqualTo("ConceptID, INTEGER, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "DimensionID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "DomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mMetric") {
                val table = MetricTable
                assertThat(table.tableName).isEqualTo("mMetric")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(9)
                assertThat(meta.columnConfigToString(1)).isEqualTo("MetricID, INTEGER, NonNullable, false")
                assertThat(meta.columnConfigToString(2)).isEqualTo("CorrespondingMemberID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(3)).isEqualTo("DataType, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(4)).isEqualTo("FlowType, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(5)).isEqualTo("BalanceType, VARCHAR, Nullable, false")
                assertThat(meta.columnConfigToString(6)).isEqualTo("ReferencedDomainID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(7)).isEqualTo("ReferencedHierarchyID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(8)).isEqualTo("HierarchyStartingMemberID, INTEGER, Nullable, false")
                assertThat(meta.columnConfigToString(9)).isEqualTo("IsStartingMemberIncluded, BOOLEAN, Nullable, false")

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "MetricID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ReferencedDomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ReferencedHierarchyID, mHierarchy, HierarchyID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "HierarchyStartingMemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "CorrespondingMemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            }
        )
    }

    private fun resultSetMetaDataOfVerifiedTable(
        dbConnection: Connection,
        table: Table
    ): ResultSetMetaData {
        val rs = dbConnection.createStatement().executeQuery("SELECT * FROM ${table.tableName}")
        return rs.metaData
    }

    private fun primaryKeysOfVerifiedTable(
        dbConnection: Connection,
        table: Table
    ): List<String> {
        val separator = ", "
        val rows = mutableListOf<String>()

        val pKeys = dbConnection.metaData.getPrimaryKeys(null, null, table.tableName)
        while (pKeys.next()) {

            val sb = StringBuilder()

            sb.append(pKeys.getString("COLUMN_NAME"))
            sb.append(separator)
            sb.append(pKeys.getInt("KEY_SEQ"))

            rows.add(sb.toString())
        }
        return rows
    }

    private fun foreignKeysOfVerifiedTable(
        dbConnection: Connection,
        table: Table
    ): List<String> {

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

        val pKeys = dbConnection.metaData.getImportedKeys(null, null, table.tableName)
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
