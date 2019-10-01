package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.ResultSet

internal class DpmDbWriter_DictionaryReplace_OpenAxisValueRestrictionTransform_ModuleTest :
    DpmDbWriter_DictionaryReplaceModuleTestBase() {

    @Test
    fun `OpenAxisValueRestriction should get updated`() {
        insertCommonBaselineFixtures()

        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
            (
            101,   -- AxisID should remain as-is
            5232,  -- Refers to Hierarchy "ExpDomHier-2-Code" within Domain "ExpDom-2-Code" => Hierarchy gets ID 5 from DictionaryFixture
            5202,  -- Refers to Member "Mbr-2-Code" within Domain "ExpDom-2-Code" => Member gets ID 7 from DictionaryFixture
            1      -- IsStartingMemberIncluded should remain as-is
            );
            """.trimIndent()
        )

        dumpDiagnosticsWhenThrown { replaceDictionaryInDb(FixtureVariety.THREE_EXPLICIT_DOMAINS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES) }

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

    @Nested
    inner class InitialValueErrors {

        @Test
        fun `NULL AxisID should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (null, 5232, 5202, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: null]",
                "VALIDATION [BaselineOpenAxisValueRestriction.axisId: does not have value]",
                "MESSAGE [INFO] [OpenAxisValueRestriction baseline loading failed]"
            )
        }

        @Test
        fun `HierarchyID having non existing ID should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 444, 5202, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.domainXbrlCode: does not have value]",
                "VALIDATION [BaselineOpenAxisValueRestriction.hierarchyCode: does not have value]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction baseline loading failed]"
            )
        }

        @Test
        fun `HierarchyStartingMemberID having non existing ID should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5232, 555, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "VALIDATION [BaselineOpenAxisValueRestriction.startingMemberXbrlCode: does not have value]",
                "MESSAGE [INFO] [OpenAxisValueRestriction baseline loading failed]"
            )
        }

        @Test
        fun `HierarchyStartingMemberID referring to member not part of hierarchy should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5132, 5101, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction baseline loading failed]"
            )
        }

        @Test
        fun `NULL IsStartingMemberIncluded should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5232, 5202, null);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [BaselineOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [BaselineOpenAxisValueRestriction.isStartingMemberIncluded: does not have value]",
                "MESSAGE [INFO] [OpenAxisValueRestriction baseline loading failed]"
            )
        }
    }

    @Nested
    inner class TransformationErrors {

        @Test
        fun `Hierarchy Domain missing from replaced dictionary should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5431, 5401, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.domainId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyStartingMemberId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction transformation failed]"
            )
        }

        @Test
        fun `Hierarchy missing from replaced dictionary should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5134, 5102, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction transformation failed]"
            )
        }

        @Test
        fun `Member missing from replaced dictionary should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5132, 5104, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.hierarchyStartingMemberId: does not have value]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction transformation failed]"
            )
        }

        @Test
        fun `Member not belonging to hierarchy should cause error`() {
            insertCommonBaselineFixtures()

            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOpenAxisValueRestriction(AxisID, HierarchyID, HierarchyStartingMemberID, IsStartingMemberIncluded) VALUES
                (101, 5132, 5106, 1);
                """.trimIndent()
            )

            ensureHaltThrown { replaceDictionaryInDb() }

            assertThat(diagnosticCollector.events).containsSequence(
                "VALIDATED OBJECT [FinalOpenAxisValueRestriction] [AxisID: 101]",
                "VALIDATION [FinalOpenAxisValueRestriction.isStartingMemberPartOfHierarchy: is not part of hierarchy]",
                "MESSAGE [INFO] [OpenAxisValueRestriction transformation failed]"
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

    private fun insertCommonBaselineFixtures() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mDomain(DomainID, DomainCode, DomainXBRLCode, IsTypedDomain) VALUES
            (51, "ExpDom-1-Code", "FixPrfx_exp:ExpDom-1-Code", 0),
            (52, "ExpDom-2-Code", "FixPrfx_exp:ExpDom-2-Code", 0),
            (53, "ExpDom-3-Code", "FixPrfx_exp:ExpDom-3-Code", 0),
            (54, "ExpDom-X-Code", "FixPrfx_exp:ExpDom-X-Code", 0);

            INSERT INTO mMember(MemberID, DomainID, MemberCode, MemberXBRLCode) VALUES
            (5101, 51, "Mbr-1-Code", "FixPrfx_ExpDom-1-Code:Mbr-1-Code"),
            (5102, 51, "Mbr-2-Code", "FixPrfx_ExpDom-1-Code:Mbr-2-Code"),
            (5103, 51, "Mbr-3-Code", "FixPrfx_ExpDom-1-Code:Mbr-3-Code"),
            (5104, 51, "Mbr-X-Code", "FixPrfx_ExpDom-1-Code:Mbr-X-Code"),
            (5106, 51, "Mbr-6-Code", "FixPrfx_ExpDom-1-Code:Mbr-6-Code"),

            (5201, 52, "Mbr-1-Code", "FixPrfx_ExpDom-2-Code:Mbr-1-Code"),
            (5202, 52, "Mbr-2-Code", "FixPrfx_ExpDom-2-Code:Mbr-2-Code"),
            (5203, 52, "Mbr-3-Code", "FixPrfx_ExpDom-2-Code:Mbr-3-Code"),

            (5301, 53, "Mbr-1-Code", "FixPrfx_ExpDom-3-Code:Mbr-1-Code"),
            (5302, 53, "Mbr-2-Code", "FixPrfx_ExpDom-3-Code:Mbr-2-Code"),
            (5303, 53, "Mbr-3-Code", "FixPrfx_ExpDom-3-Code:Mbr-3-Code"),

            (5401, 54, "Mbr-1-Code", "FixPrfx_ExpDom-X-Code:Mbr-1-Code");


            INSERT INTO mHierarchy(HierarchyID, HierarchyCode, DomainID) VALUES
            (5131, "ExpDomHier-1-Code", 51),
            (5132, "ExpDomHier-2-Code", 51),
            (5133, "ExpDomHier-3-Code", 51),
            (5134, "ExpDomHier-X-Code", 51),

            (5231, "ExpDomHier-1-Code", 52),
            (5232, "ExpDomHier-2-Code", 52),
            (5233, "ExpDomHier-3-Code", 52),

            (5331, "ExpDomHier-1-Code", 53),
            (5332, "ExpDomHier-2-Code", 53),
            (5333, "ExpDomHier-3-Code", 53),

            (5431, "ExpDomHier-X-Code", 54);

            INSERT INTO mHierarchyNode(HierarchyID, MemberID) VALUES
            (5232, 5202),
            (5232, 5203),

            (5431, 5401),
            (5134, 5102),
            (5132, 5104),
            (5132, 5106);

            """.trimIndent()
        )
    }
}
