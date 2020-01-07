package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.sqliteoutput.tables.AxisOrdinateTable
import fi.vm.yti.taxgen.sqliteoutput.tables.AxisTable
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteoutput.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteoutput.tables.DomainTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyNodeTable
import fi.vm.yti.taxgen.sqliteoutput.tables.HierarchyTable
import fi.vm.yti.taxgen.sqliteoutput.tables.LanguageTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MemberTable
import fi.vm.yti.taxgen.sqliteoutput.tables.MetricTable
import fi.vm.yti.taxgen.sqliteoutput.tables.OrdinateCategorisationTable
import fi.vm.yti.taxgen.sqliteoutput.tables.OwnerTable
import fi.vm.yti.taxgen.sqliteoutput.tables.Tables
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.columnConfigs
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
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicNode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class DpmDbWriter_TableStructure_ModuleTest {

    enum class StructureInitMode {
        SELF_INITIALIZED,
        SEED_BASED
    }

    private lateinit var tempFolder: TempFolder
    private lateinit var seedBasedDbConnection: Connection
    private lateinit var selfCreatedDbConnection: Connection

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_structure")
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

        return createTableStructureTests(
            seedBasedDbConnection,
            StructureInitMode.SEED_BASED
        )
    }

    @TestFactory
    fun `Verify structure with self initialized database`(): List<DynamicNode> {
        val dbPath = tempFolder.resolve("self_created_dpm.db")

        Database.connect("jdbc:sqlite:$dbPath", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        Tables.create()

        selfCreatedDbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        return createTableStructureTests(
            selfCreatedDbConnection,
            StructureInitMode.SELF_INITIALIZED
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun createTableStructureTests(
        dbConnection: Connection,
        initMode: StructureInitMode
    ): List<DynamicNode> {

        return listOf(
            DynamicTest.dynamicTest("mLanguage") {
                val table = LanguageTable
                assertThat(table.tableName).isEqualTo("mLanguage")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "LanguageID, INTEGER, NonNullable, false",
                    "LanguageName, VARCHAR, Nullable, false",
                    "EnglishName, VARCHAR, Nullable, false",
                    "IsoCode, VARCHAR, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "ConceptID, INTEGER, NonNullable, false",
                    "ConceptType, VARCHAR, Nullable, false",
                    "OwnerID, INTEGER, Nullable, false",
                    "CreationDate, DATE, Nullable, false",
                    "ModificationDate, DATE, Nullable, false",
                    "FromDate, DATE, Nullable, false",
                    "ToDate, DATE, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "ConceptID, INTEGER, NonNullable, false",
                    "LanguageID, INTEGER, NonNullable, false",
                    "Text, VARCHAR, Nullable, false",
                    "Role, VARCHAR, NonNullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "OwnerID, INTEGER, NonNullable, false",
                    "OwnerName, VARCHAR, Nullable, false",
                    "OwnerNamespace, VARCHAR, Nullable, false",
                    "OwnerLocation, VARCHAR, Nullable, false",
                    "OwnerPrefix, VARCHAR, Nullable, false",
                    "OwnerCopyright, VARCHAR, Nullable, false",
                    "ParentOwnerID, INTEGER, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "DomainID, INTEGER, NonNullable, false",
                    "DomainCode, VARCHAR, Nullable, false",
                    "DomainLabel, VARCHAR, Nullable, false",
                    "DomainDescription, VARCHAR, Nullable, false",
                    "DomainXBRLCode, VARCHAR, Nullable, false",
                    "DataType, VARCHAR, Nullable, false",
                    "IsTypedDomain, BOOLEAN, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "MemberID, INTEGER, NonNullable, false",
                    "DomainID, INTEGER, Nullable, false",
                    "MemberCode, VARCHAR, Nullable, false",
                    "MemberLabel, VARCHAR, Nullable, false",
                    "MemberXBRLCode, VARCHAR, Nullable, false",
                    "IsDefaultMember, BOOLEAN, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement", "HierarchyID, INTEGER, NonNullable, false",
                    "HierarchyCode, VARCHAR, Nullable, false",
                    "HierarchyLabel, VARCHAR, Nullable, false",
                    "DomainID, INTEGER, Nullable, false",
                    "HierarchyDescription, VARCHAR, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "HierarchyID, INTEGER, NonNullable, false",
                    "MemberID, INTEGER, NonNullable, false",
                    "IsAbstract, BOOLEAN, Nullable, false",
                    "ComparisonOperator, VARCHAR, Nullable, false",
                    "UnaryOperator, VARCHAR, Nullable, false",
                    "Order, INTEGER, Nullable, false",
                    "Level, INTEGER, Nullable, false",
                    "ParentMemberID, INTEGER, Nullable, false",
                    "HierarchyNodeLabel, VARCHAR, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false",
                    "Path, VARCHAR, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "DimensionID, INTEGER, NonNullable, false",
                    "DimensionLabel, VARCHAR, Nullable, false",
                    "DimensionCode, VARCHAR, Nullable, false",
                    "DimensionDescription, VARCHAR, Nullable, false",
                    "DimensionXBRLCode, VARCHAR, Nullable, false",
                    "DomainID, INTEGER, Nullable, false",
                    "IsTypedDimension, BOOLEAN, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

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

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "MetricID, INTEGER, NonNullable, false",
                    "CorrespondingMemberID, INTEGER, Nullable, false",
                    "DataType, VARCHAR, Nullable, false",
                    "FlowType, VARCHAR, Nullable, false",
                    "BalanceType, VARCHAR, Nullable, false",
                    "ReferencedDomainID, INTEGER, Nullable, false",
                    "ReferencedHierarchyID, INTEGER, Nullable, false",
                    "HierarchyStartingMemberID, INTEGER, Nullable, false",
                    "IsStartingMemberIncluded, BOOLEAN, Nullable, false"
                )

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "MetricID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "ReferencedDomainID, mDomain, DomainID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ReferencedHierarchyID, mHierarchy, HierarchyID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "HierarchyStartingMemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "CorrespondingMemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mAxis") {
                val table = AxisTable
                assertThat(table.tableName).isEqualTo("mAxis")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "AxisID, INTEGER, NonNullable, false",
                    "AxisOrientation, VARCHAR, Nullable, false",
                    "AxisLabel, VARCHAR, Nullable, false",
                    "IsOpenAxis, BOOLEAN, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false"
                )

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "AxisID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder()
            },

            DynamicTest.dynamicTest("mAxisOrdinate") {
                val table = AxisOrdinateTable
                assertThat(table.tableName).isEqualTo("mAxisOrdinate")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnCount).isEqualTo(12)

                assertThat(meta.columnConfigs()).containsExactlyInAnyOrder(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "AxisID, INTEGER, Nullable, false",
                    "OrdinateID, INTEGER, NonNullable, false",

                    "OrdinateLabel, VARCHAR, Nullable, false",
                    "OrdinateCode, VARCHAR, Nullable, false",
                    "IsDisplayBeforeChildren, BOOLEAN, Nullable, false",
                    "IsAbstractHeader, BOOLEAN, Nullable, false",
                    "IsRowKey, BOOLEAN, Nullable, false",

                    "Level, INTEGER, Nullable, false",
                    "Order, INTEGER, Nullable, false",
                    "ParentOrdinateID, INTEGER, Nullable, false",
                    "ConceptID, INTEGER, Nullable, false",

                    "TypeOfKey, VARCHAR, Nullable, false"
                )

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "OrdinateID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "AxisID, mAxis, AxisID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ParentOrdinateID, mAxisOrdinate, OrdinateID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "ConceptID, mConcept, ConceptID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
                )
            },

            DynamicTest.dynamicTest("mOrdinateCategorisation") {
                val table = OrdinateCategorisationTable
                assertThat(table.tableName).isEqualTo("mOrdinateCategorisation")

                val meta = resultSetMetaDataOfVerifiedTable(dbConnection, table)

                assertThat(meta.columnConfigs()).containsExactly(
                    "#ColumnName, #ColumnType, #Nullable, #AutoIncrement",
                    "OrdinateID, INTEGER, NonNullable, false",
                    "DimensionID, INTEGER, NonNullable, false",
                    "MemberID, INTEGER, Nullable, false",
                    "DimensionMemberSignature, VARCHAR, Nullable, false",
                    "Source, VARCHAR, Nullable, false",
                    "DPS, VARCHAR, Nullable, false"
                )

                assertThat(primaryKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "DimensionID, 2", "OrdinateID, 1"
                )

                assertThat(foreignKeysOfVerifiedTable(dbConnection, table)).containsExactlyInAnyOrder(
                    "OrdinateID, mAxisOrdinate, OrdinateID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "DimensionID, mDimension, DimensionID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred",
                    "MemberID, mMember, MemberID, 1, UPDATE_RULE:NoAction, DELETE_RULE:NoAction, DEFERRABILITY:InitiallyDeferred"
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
