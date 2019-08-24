package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

internal class SQLiteProvider_DictionaryReplace_UnitTest {

    private lateinit var tempFolder: TempFolder
    private lateinit var dbPath: Path
    private lateinit var dbConnection: Connection
    private lateinit var diagnosticCollector: DiagnosticCollector

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider_replace")
        dbPath = tempFolder.resolve("replace_dpm_dictionary.db")

        val stream = this::class.java.getResourceAsStream("/db_fixture/plain_dictionary.db")
        Files.copy(stream, dbPath, StandardCopyOption.REPLACE_EXISTING)

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")

        diagnosticCollector = DiagnosticCollector()
    }

    private fun replaceDictionaryInDb(variety: FixtureVariety = FixtureVariety.NONE) {
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)
        val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
            dbPath,
            diagnosticContext
        )

        val model = dpmModelFixture(variety)

        dbWriter.writeModel(model)
    }

    private fun dumpDiagnosticsWhenThrown(action: () -> Unit) {

        val thrown = catchThrowable { action() }

        if (thrown != null) {
            println(diagnosticCollector.events)
            throw thrown
        }
    }

    private fun ensureHaltThrown(action: () -> Unit) {
        val thrown = catchThrowable { action() }
        assertThat(thrown).isInstanceOf(HaltException::class.java)
    }

    @AfterEach
    fun baseTeardown() {
        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }

        tempFolder.close()
    }

    @Test
    fun `should fail when target DB is missing required Eurofiling owner`() {
        dbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "eu"
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting 'Eurofiling' Owner from database failed. Found 0 Owners with prefix 'eu'.]"
        )
    }

    @Test
    fun `should fail when target DB is missing owner of the dictionary`() {
        dbConnection.createStatement().executeUpdate(
            """
            DELETE FROM mOwner WHERE mOwner.OwnerPrefix = "FixPrfx"
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Selecting Owner from database failed. Found 0 Owners with prefix 'FixPrfx'.]"
        )
    }

    @Nested
    inner class OrdinateCategorisationTransformation {

        @Test
        fun `ordinate categorisation referring explicit dimension and explicit domain member should get updated`() {
            dbConnection.createStatement().executeUpdate(
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
                dbConnection.createStatement().executeUpdate(
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
                dbConnection.createStatement().executeUpdate(
                    """
                    INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                    VALUES (111, 222, 333, "FixPrfx_dim:ExpDim-2-Code(*[444;555;1])", "source", "FixPrfx_dim:ExpDim-2-Code(*[ExpDomHier-3-Code;Mbr-2-Code;0])")
                    """.trimIndent()
                )

                dumpDiagnosticsWhenThrown {
                    replaceDictionaryInDb( FixtureVariety.THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES)
                }

                val rs = readAllOrdinateCategorisations()

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                    "111, 2, 9999, FixPrfx_dim:ExpDim-2-Code(*[6;7;0]), source, FixPrfx_dim:ExpDim-2-Code(*[ExpDomHier-3-Code;Mbr-2-Code;0])"
                )
            }
        }

        private fun readAllOrdinateCategorisations(): ResultSet {
            return dbConnection.createStatement().executeQuery(
                """
                SELECT * FROM mOrdinateCategorisation
                """.trimIndent()
            )
        }

        @Nested
        inner class SignatureTransformation {

            private fun insertCategorisationWithDimensionMemberSignature(dmsValue: String) {
                diagnosticCollector.reset()

                dbConnection.createStatement().executeUpdate(
                    """
                    DELETE FROM mOrdinateCategorisation;

                    INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
                    VALUES (111, 222, 333, "$dmsValue", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
                    """.trimIndent()
                )
            }

            private fun insertCategorisationWithDps(dpsValue: String) {
                diagnosticCollector.reset()

                dbConnection.createStatement().executeUpdate(
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
                        "VALIDATION [Signature.dimensionIdentifier: is blank]"
                    )

                    //DPS
                    insertCategorisationWithDps(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                        "VALIDATION [Signature.dimensionIdentifier: is blank]"
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
                        "VALIDATION [Signature.memberIdentifier: is blank]"
                    )

                    //DPS
                    insertCategorisationWithDps(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111]",
                        "VALIDATION [Signature.memberIdentifier: is blank]"
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
                        "VALIDATION [Relationships.dimensionId: does not have value]"
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
                        "VALIDATION [Relationships.memberId: does not have value]"
                    )
                }

                @Test
                fun `unknown hierarchy in DPS should cause error`() {
                    val signature = "FixPrfx_dim:ExpDim-1-Code(*[NonExistingHierarchy;Mbr-2-Code;0])"

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
                        "VALIDATION [OpenAxisValueRestrictionRelationships.hierarchyId: does not have value]",
                        "VALIDATION [OpenAxisValueRestrictionRelationships.hierarchyStartingMemberId: does not have value]"
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
                        "VALIDATION [OpenAxisValueRestrictionRelationships.hierarchyStartingMemberId: does not have value]"
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
}
