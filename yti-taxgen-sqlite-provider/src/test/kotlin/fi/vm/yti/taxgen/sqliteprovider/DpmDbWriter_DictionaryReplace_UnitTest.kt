package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.HaltException
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.testcommons.DiagnosticCollector
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager

@DisplayName("SQLite DPM DB: dictionary replace")
internal class DpmDbWriter_DictionaryReplace_UnitTest {

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

        val model = DpmModel(
            dictionaries = listOf(dpmDictionaryFixture(FixtureVariety.NONE))
        )

        dbWriter.writeModel(model)
    }

    @AfterEach
    fun baseTeardown() {
        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }

        tempFolder.close()
    }

    @Test
    fun `should fail when target DB is missing eurofiling owner`() {
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

    @Test
    fun `should fail when ordinate categorisation DPS has extra parentheses`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:ExpDim-1-Code((FixPrfx_ExpDom-1-Code:Mbr-1-Code))")
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.events).contains(
            "MESSAGE [FATAL] [Unsupported DPS structure]"
        )
    }

    @Test
    fun `should fail when ordinate categorisation signature has extra trailing part`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)(foo)")
            """
        )

        val thrown = catchThrowable { replaceDictionaryInDb() }

        assertThat(thrown).isInstanceOf(HaltException::class.java)

        assertThat(diagnosticCollector.eventsString()).contains(
            "MESSAGE [FATAL] [Unsupported DPS structure]"
        )
    }

    @Test
    fun `should fail when ordinate categorisation signature refers to unknown dimension`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:NonExistingDimension(FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
            """
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).containsSubsequence(
            "VALIDATED OBJECT [OrdinateCategorisation] [OrdinateID: 1122, DPS: FixPrfx_dim:NonExistingDimension(FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
            "VALIDATION [OrdinateCategorisationBindingData.dimensionId: does not have value]"
        )
    }

    @Test
    fun `should fail when ordinate categorisation signature is blank`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", " (FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
            """
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).containsSubsequence(
            "VALIDATED OBJECT [OrdinateCategorisation] [OrdinateID: 1122, DPS:  (FixPrfx_ExpDom-1-Code:Mbr-1-Code)]",
            "VALIDATION [OrdinateCategorisationBindingData.dimensionId: does not have value]",
            "VALIDATION [OrdinateCategorisationBindingData.dpsDimensionXbrlCode: is blank]"
        )
    }

    @Test
    fun `should fail when ordinate categorisation signature refers to unknown member`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:NonExistingMember)")
            """
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).contains(
            "VALIDATION [OrdinateCategorisationBindingData.memberId: does not have value]"
        )
    }

    @Test
    fun `should fail when ordinate categorisation signature member is blank`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:ExpDim-1-Code( )")
            """
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).contains(
            "VALIDATION [OrdinateCategorisationBindingData.dpsMemberXbrlCode: is blank]",
            "VALIDATION [OrdinateCategorisationBindingData.memberId: does not have value]"
        )
    }

    @Test
    fun `should update simple ordinate categorisation`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)")
            """.trimIndent()
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).containsExactly(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = dbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mOrdinateCategorisation
            """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
            "1122, 1, 1, signature, source, FixPrfx_dim:ExpDim-1-Code(FixPrfx_ExpDom-1-Code:Mbr-1-Code)"
        )
    }

    @Test
    fun `should update typed dimension with open member ordinate categorisation`() {
        dbConnection.createStatement().executeUpdate(
            """
            INSERT INTO mOrdinateCategorisation(OrdinateID, DimensionID, MemberID, DimensionMemberSignature, Source, DPS)
            VALUES (1122, 3344, 5566, "signature", "source", "FixPrfx_dim:TypDim-1-Code(*)")
            """.trimIndent()
        )

        replaceDictionaryInDb()

        assertThat(diagnosticCollector.events).contains(
            "ENTER [SQLiteDbWriter] []",
            "EXIT [SQLiteDbWriter]"
        )

        val rs = dbConnection.createStatement().executeQuery(
            """
            SELECT * FROM mOrdinateCategorisation
            """
        )

        assertThat(rs.toStringList()).containsExactlyInAnyOrder(
            "#OrdinateID, #DimensionID, #MemberID, #DimensionMemberSignature, #Source, #DPS",
            "1122, 2, 9999, signature, source, FixPrfx_dim:TypDim-1-Code(*)"
        )
    }
}
