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

    private fun replaceDictionaryInDb() {
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)
        val dbWriter = DpmDbWriterFactory.dictionaryReplaceWriter(
            dbPath,
            diagnosticContext
        )

        val model = dpmModelFixture(FixtureVariety.NONE)

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
                VALUES (111, 222, 333, "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
                """.trimIndent()
            )

            replaceDictionaryInDb()

            assertThat(diagnosticCollector.events).containsExactly(
                "ENTER [SQLiteDbWriter] []",
                "EXIT [SQLiteDbWriter]"
            )

            val rs = readAllOrdinateCategorisations()

            assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                "111, 1, 1, FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code), source, FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)"
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
                VALUES (111, 222, 333, "fi_dim:PAL(*[444;555;666])", "source", "FixPrfx_dim:ExpDim-1-Code(*[PA1;x0;0])")
                """.trimIndent()
                )

                replaceDictionaryInDb()

                val rs = readAllOrdinateCategorisations()

                assertThat(rs.toStringList()).containsExactlyInAnyOrder(
                    "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
                    "111, 1, 9999, fi_dim:PAL(*[444;555;666]), source, FixPrfx_dim:ExpDim-1-Code(*[PA1;x0;0])"
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
                fun `blank dimension part should cause error`() {
                    val faultySignature = " (FixPrfx_ExpDom-1-Code:Mbr-1-Code)"

                    //DimensionMemberSignature
                    insertCategorisationWithDimensionMemberSignature(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).contains(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111, DPS: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
                        "VALIDATION [Signature.dimensionIdentifier: is blank]"
                    )

                    //DPS
                    insertCategorisationWithDps(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).contains(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111, DPS:  (FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
                        "VALIDATION [Signature.dimensionIdentifier: is blank]"
                    )
                }

                @Test
                fun `blank member should cause error`() {
                    val faultySignature = "FixPrfx_dim:ExpDim-1-Code( )"

                    //DimensionMemberSignature
                    insertCategorisationWithDimensionMemberSignature(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).contains(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111, DPS: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
                        "VALIDATION [Signature.memberIdentifier: is blank]"
                    )

                    //DPS
                    insertCategorisationWithDps(faultySignature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).contains(
                        "VALIDATED OBJECT [BaselineOrdinateCategorisation] [OrdinateID: 111, DPS: FixPrfx_dim:ExpDim-1-Code( )]",
                        "VALIDATION [Signature.memberIdentifier: is blank]"
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

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "ENTER [SQLiteDbWriter] []",
                        "EXIT [SQLiteDbWriter]"
                    )

                    //DPS
                    insertCategorisationWithDps(signature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "VALIDATED OBJECT [OrdinateCategorisation] [OrdinateID: 111, DPS: FixPrfx_dim:NonExistingDimension(FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
                        "VALIDATION [FinalOrdinateCategorisation.dimensionId: does not have value]"
                    )
                }

                @Test
                fun `unknown member in DPS should cause error`() {
                    val signature = "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:NonExistingMember)"

                    //DimensionMemberSignature
                    insertCategorisationWithDimensionMemberSignature(signature)
                    replaceDictionaryInDb()

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "ENTER [SQLiteDbWriter] []",
                        "EXIT [SQLiteDbWriter]"
                    )

                    //DPS
                    insertCategorisationWithDps(signature)
                    ensureHaltThrown { replaceDictionaryInDb() }

                    assertThat(diagnosticCollector.events).containsSubsequence(
                        "VALIDATED OBJECT [OrdinateCategorisation] [OrdinateID: 111, DPS: FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:NonExistingMember)]",
                        "VALIDATION [FinalOrdinateCategorisation.memberId: does not have value]"
                    )
                }
            }
        }
    }
}
