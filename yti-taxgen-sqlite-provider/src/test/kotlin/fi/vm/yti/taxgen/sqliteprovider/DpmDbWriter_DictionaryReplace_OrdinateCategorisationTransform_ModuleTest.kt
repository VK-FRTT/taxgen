package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.sql.ResultSet

internal class DpmDbWriter_DictionaryReplace_OrdinateCategorisationTransform_ModuleTest :
    DpmDbWriter_DictionaryReplaceModuleTestBase() {

    @Test
    fun `ordinate categorisation referring explicit dimension and explicit domain member should get updated`() {
        baselineDbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (111, 222, 333, "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)")
            """.trimIndent()
        )

        dumpDiagnosticsWhenThrown { replaceDictionaryInDb() }

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = readAllOrdinateCategorisations()

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
            "111, 1, 2, FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code), source, FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-2-Code)"
        )
    }

    @Test
    fun `ordinate categorisation referring typed dimension and open member should get updated`() {
        dumpDiagnosticsWhenThrown {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, 222, 333, "FixPrfx_dim:TypDim-1-Code(*)", "source", "FixPrfx_dim:TypDim-1-Code(*)")
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).contains(
                "ENTER [SQLiteDbWriter] []",
                "EXIT [SQLiteDbWriter]"
            )

            val rs = readAllOrdinateCategorisations()

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, 2, 9999, FixPrfx_dim:TypDim-1-Code(*), source, FixPrfx_dim:TypDim-1-Code(*)"
            )
        }
    }

    @Test
    fun `ordinate categorisation having OpenAxisValueRestriction should get updated`() {
        dumpDiagnosticsWhenThrown {
            baselineDbConnection.createStatement().executeUpdate(
                """
                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, 222, 333, "FixPrfx_dim:ExpDim-2-Code(*[444;555;1])", "source", "FixPrfx_dim:ExpDim-2-Code(*[ExpDomHier-2-Code;Mbr-2-Code;0])")
                """.trimIndent()
            )

            dumpDiagnosticsWhenThrown {
                replaceDictionaryInDb(FixtureVariety.THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES)
            }

            val rs = readAllOrdinateCategorisations()

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, 2, 9999, FixPrfx_dim:ExpDim-2-Code(*[5;8;0]), source, FixPrfx_dim:ExpDim-2-Code(*[ExpDomHier-2-Code;Mbr-2-Code;0])"
            )
        }
    }

    private fun readAllOrdinateCategorisations(): ResultSet {
        return outputDbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mOrdinateCategorisation
            """.trimIndent()
        )
    }

    @Nested
    inner class SignatureTransformation {

        private fun insertCategorisationWithDimensionMemberSignature(dmsValue: String) {
            diagnosticCollector.reset()

            baselineDbConnection.createStatement().executeUpdate(
                """
                DELETE FROM mOrdinateCategorisation;

                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, 222, 333, "$dmsValue", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
                """.trimIndent()
            )
        }

        private fun insertCategorisationWithDps(dpsValue: String) {
            diagnosticCollector.reset()

            baselineDbConnection.createStatement().executeUpdate(
                """
                DELETE FROM mOrdinateCategorisation;

                INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                VALUES (111, 222, 333, "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)", "source", "$dpsValue")
                """.trimIndent()
            )
        }

        @Nested
        inner class SignatureStructureErrors {

            @Test
            fun `extra parentheses around member reference should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code((FixPrfx_ExpDom-1-Code:Mbr-1-Code))"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DimensionMemberSignature: FixPrfx_dim:ExpDim-1-Code((FixPrfx_ExpDom-1-Code:Mbr-1-Code))]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DPS: FixPrfx_dim:ExpDim-1-Code((FixPrfx_ExpDom-1-Code:Mbr-1-Code))]"
                )
            }

            @Test
            fun `extra trailing part should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)(foo)"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DimensionMemberSignature: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)(foo)]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).contains(
                    "MESSAGE [FATAL] [Unsupported signature in OrdinateCategorisation.DPS: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)(foo)]"
                )
            }

            @Test
            fun `blank dimension value should cause error`() {
                val faultySignature = " (FixPrfx_ExpDom-1-Code:Mbr-1-Code)"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationSignature.dimensionIdentifier: is blank]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationSignature.dimensionIdentifier: is blank]"
                )
            }

            @Test
            fun `blank member value should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code( )"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationSignature.memberIdentifier: is blank]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationSignature.memberIdentifier: is blank]"
                )
            }

            @Test
            fun `blank hierarchy value should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code(*[ ;x0;0])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.hierarchyIdentifier: is blank]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.hierarchyIdentifier: is blank]"
                )
            }

            @Test
            fun `blank hierarchy starting member value should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code(*[ExpDomHier-3-Code; ;0])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.hierarchyStartingMemberIdentifier: is blank]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.hierarchyStartingMemberIdentifier: is blank]"
                )
            }

            @Test
            fun `blank starting member inclusion marker should cause error`() {
                val faultySignature = "FixPrfx_dim:ExpDim-1-Code(*[ExpDomHier-3-Code;Mbr-2-Code; ])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.startingMemberIncluded: is blank]"
                )

                //DPS
                insertCategorisationWithDps(faultySignature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.startingMemberIncluded: is blank]"
                )
            }
        }

        @Nested
        inner class SignatureValueErrors {

            @Test
            fun `unknown dimension in DPS should cause error`() {
                val signature = "FixPrfx_dim:NonExistingDimension(FixPrfx_ExpDom-1-Code:Mbr-1-Code)"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(signature)
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).doesNotContain(
                    "fail"
                )

                assertThat(diagnosticCollector.events).containsSequence(
                    "ENTER [SQLiteDbWriter] []",
                    "EXIT [SQLiteDbWriter]"
                )

                //DPS
                insertCategorisationWithDps(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [FinalOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationDbReferences.dimensionId: does not have value]"
                )
            }

            @Test
            fun `unknown member in DPS should cause error`() {
                val signature = "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:NonExistingMember)"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(signature)
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).doesNotContain(
                    "fail"
                )

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "ENTER [SQLiteDbWriter] []",
                    "EXIT [SQLiteDbWriter]"
                )

                //DPS
                insertCategorisationWithDps(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [FinalOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OrdinateCategorisationDbReferences.memberId: does not have value]"
                )
            }

            @Test
            fun `unknown hierarchy in DPS should cause error`() {
                val signature = "FixPrfx_dim:ExpDim-1-Code(*[NonExistingHierarchy;Mbr-2-Code;0])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(signature)
                dumpDiagnosticsWhenThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).doesNotContain(
                    "fail"
                )

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "ENTER [SQLiteDbWriter] []",
                    "EXIT [SQLiteDbWriter]"
                )

                //DPS
                insertCategorisationWithDps(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSequence(
                    "VALIDATED OBJECT [FinalOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionDbReferences.hierarchyId: does not have value]",
                    "VALIDATION [OpenAxisValueRestrictionDbReferences.hierarchyStartingMemberId: does not have value]"
                )
            }

            @Test
            fun `unknown hierarchy starting member in DPS should cause error`() {
                val signature = "FixPrfx_dim:ExpDim-1-Code(*[ExpDomHier-3-Code;NonExistingMember;0])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(signature)
                replaceDictionaryInDb()

                assertThat(diagnosticCollector.events).doesNotContain(
                    "fail"
                )

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "ENTER [SQLiteDbWriter] []",
                    "EXIT [SQLiteDbWriter]"
                )

                //DPS
                insertCategorisationWithDps(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSequence(
                    "VALIDATED OBJECT [FinalOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionDbReferences.hierarchyStartingMemberId: does not have value]"
                )
            }

            @Test
            fun `unsupported starting member inclusion marker should cause error`() {
                val signature = "FixPrfx_dim:ExpDim-1-Code(*[ExpDomHier-3-Code;Mbr-2-Code;XYZ])"

                //DimensionMemberSignature
                insertCategorisationWithDimensionMemberSignature(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSubsequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value 'XYZ']"
                )

                //DPS
                insertCategorisationWithDps(signature)
                ensureHaltThrown { replaceDictionaryInDb() }

                assertThat(diagnosticCollector.events).containsSequence(
                    "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                    "VALIDATION [OpenAxisValueRestrictionSignature.startingMemberIncluded: unsupported IsStartingMemberIncluded value 'XYZ']"
                )
            }
        }
    }
}
