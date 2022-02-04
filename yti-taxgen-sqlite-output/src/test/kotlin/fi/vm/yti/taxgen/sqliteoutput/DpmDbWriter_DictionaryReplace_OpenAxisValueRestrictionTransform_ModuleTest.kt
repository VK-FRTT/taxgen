package fi.vm.yti.taxgen.sqliteoutput

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import java.sql.ResultSet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class DpmDbWriter_DictionaryReplace_OpenAxisValueRestrictionTransform_ModuleTest :
    DpmDbWriter_DictionaryReplaceModuleTestBase() {

    @Test
    fun `Full OpenAxisValueRestriction should get updated`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
            (
            101,    -- AxisID should remain as-is
            14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" => Hierarchy gets ID 5 from DictionaryFixture generated DB
            26,     -- BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" => Member gets ID 7 from DictionaryFixture generated DB
            1       -- IsStartingMemberIncluded should remain as-is
            );
            """.trimIndent()
        )

        replaceDictionaryInDb(
            FixtureVariety.ONLY_ONE_DICTIONARY
        )

        assertThat(diagnosticCollector.events).containsSubsequence(
            "ENTER [SQLiteDbWriter] [Mode DictionaryReplace]",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = readAllOpenAxisValueRestrictions()

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#AxisID, #HierarchyID, #HierarchyStartingMemberID, #IsStartingMemberIncluded",
            "101, 5, 8, 1"
        )
    }

    @Test
    fun `Partial OpenAxisValueRestriction should get updated`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
            (
            101,    -- AxisID should remain as-is
            14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" within Domain "ExpDom-2-Code" in BaselineDB => Hierarchy gets ID 5 from DictionaryFixture generated DB
            null,   -- HierarchyStartingMemberID remains as-is
            0       -- IsStartingMemberIncluded should remain as-is
            );
            """.trimIndent()
        )

        replaceDictionaryInDb(
            FixtureVariety.ONLY_ONE_DICTIONARY
        )

        assertThat(diagnosticCollector.events).containsSubsequence(
            "ENTER [SQLiteDbWriter] [Mode DictionaryReplace]",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = readAllOpenAxisValueRestrictions()

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#AxisID, #HierarchyID, #HierarchyStartingMemberID, #IsStartingMemberIncluded",
            "101, 5, nil, 0"
        )
    }

    @Nested
    inner class InitialValueErrors {

        @Test
        fun `NULL AxisID should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                null,   -- Faulty value
                14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                26,     -- BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: null] [AxisId] [Value missing (null)]"
            )
        }

        @Test
        fun `HierarchyID having non existing ID should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                555,    -- Faulty value: Does not exist in BaselineDB
                26,     -- BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [DomainXbrlCode] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [HierarchyCode] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (FixPrfx_ExpDom-2-Code:Mbr-2-Code) is not part of Hierarchy (null)]"
            )
        }

        @Test
        fun `HierarchyStartingMemberID having non existing ID should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                555,    -- Faulty value: Does not exist in BaselineDB
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [HierarchyStartingMemberXbrlCode] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (null) is not part of Hierarchy (ExpDomHier-2-Code)]"
            )
        }

        @Test
        fun `HierarchyStartingMemberID referring to member not part of hierarchy should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                20,     -- Faulty value: Refers to Member "Mbr-2-Code" in Domain "ExpDom-1-Code" in BaselineDB (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (FixPrfx_ExpDom-1-Code:Mbr-2-Code) is not part of Hierarchy (ExpDomHier-2-Code)]"
            )
        }

        @Test
        fun `NULL IsStartingMemberIncluded should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,         -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                26,         -- BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                null        -- Faulty value
                );
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (baseline)] [AxisID: 101] [IsStartingMemberIncluded] [Value missing (null)]"
            )
        }
    }

    @Nested
    inner class TransformationErrors {

        @Test
        fun `Hierarchy Domain missing from replaced dictionary should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                23,     -- Faulty: BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "CFixPrfx" Owner)
                46,     -- BaselineDB: Refers to Member "Mbr-4-Code" in Domain "ExpDom-2-Code" (under "CFixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb(
                FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN
            )

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [DomainId] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [HierarchyId] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [HierarchyStartingMemberId] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (ID null) is not part of Hierarchy (ID null)]"
            )
        }

        @Test
        fun `Hierarchy missing from replaced dictionary should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,     -- Faulty: BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                26,     -- BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb(
                FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY
            )

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [HierarchyId] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (ID 26) is not part of Hierarchy (ID null)]"
            )
        }

        @Test
        fun `Member missing from replaced dictionary should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                26,     -- Faulty: BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb(
                FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_MEMBER
            )

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [HierarchyStartingMemberId] [Value missing (null)]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (ID null) is not part of Hierarchy (ID 14)]"
            )
        }

        @Test
        fun `Member not belonging to hierarchy should cause error`() {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (
                101,
                14,     -- BaselineDB: Refers to Hierarchy "ExpDomHier-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                26,     -- Faulty: BaselineDB: Refers to Member "Mbr-2-Code" in Domain "ExpDom-2-Code" (under "FixPrfx" Owner)
                1
                );
                """.trimIndent()
            )

            replaceDictionaryInDb(
                FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY_NODE
            )

            assertThat(diagnosticCollector.events).containsSubsequence(
                "ENTER [SQLiteDbWriter] [Mode DictionaryReplace]",
                "VALIDATION [OpenAxisValueRestriction (transformed)] [AxisID: 101] [IsHierarchyStartingMemberPartOfHierarchy] [HierarchyStartingMember (ID 26) is not part of Hierarchy (ID 14)]",
                "EXIT [SQLiteDbWriter]"
            )
        }
    }

    private fun readAllOpenAxisValueRestrictions(): ResultSet {
        return outputDbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mOpenAxisValueRestriction
            """.trimIndent()
        )
    }
}
