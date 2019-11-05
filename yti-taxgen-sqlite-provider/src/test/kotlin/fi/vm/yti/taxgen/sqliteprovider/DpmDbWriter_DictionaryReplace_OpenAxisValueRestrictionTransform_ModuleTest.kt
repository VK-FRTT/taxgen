package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.ResultSet

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

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
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

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
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
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: null]",
                "VALIDATION [BaselineOpenAxisValueRestriction.axisId: does not have value]"
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
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.domainXbrlCode: does not have value]",
                "VALIDATION [BaselineOpenAxisValueRestriction.hierarchyCode: does not have value]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]"
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
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "VALIDATION [BaselineOpenAxisValueRestriction.startingMemberXbrlCode: does not have value]"
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
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]"
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
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberIncluded: does not have value]"
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
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.domainId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyStartingMemberId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]"
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
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]"
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
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyStartingMemberId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]"
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

            assertThat(diagnosticCollector.events).containsSequence(
                "ENTER [SQLiteDbWriter] []",
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
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
