package fi.vm.yti.taxgen.cli

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_SOURCE_CONFIG
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.DriverManager

@DisplayName("Command ´--create-dictionary-to-new-dpm-db´")
internal class TaxgenCli_CreateDictionaryToNewDb_Test : TaxgenCli_TestBase(
    primaryCommand = "--create-dictionary-to-new-dpm-db"
) {

    private lateinit var targetDbPath: Path

    @BeforeEach
    fun init() {
        targetDbPath = tempFolder.resolve("dpm.db")
    }

    @Test
    fun `Should produce database from DPM source capture`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )
        }
    }

    @Test
    fun `Should overwrite target database file when force option is given`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--force-overwrite",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should fail when target database filename is not given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single command with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should report error when target database file already exists`() {
        Files.write(targetDbPath, "Existing file".toByteArray())

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: Target file '$targetDbPath' already exists"
            )

            assertThat(targetDbPath).exists().isRegularFile()
        }
    }

    @Test
    fun `Should report error when given target database path points to folder`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "${tempFolder.path()}",
            "--source-folder",
            "$dpmSourceCapturePath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "FATAL: Target file '${tempFolder.path()}' already exists"
            )
        }
    }

    @Test
    fun `Should fail when no source option is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Test
    fun `Should fail when source option without filepath is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder requires an argument"
            )
        }
    }

    @Test
    fun `Should fail when given source filepath does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.resolve("non_existing_folder")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).isBlank()

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-folder: Directory", "does not exist"
            )
        }
    }

    @Test
    fun `Should fail when more than one source option is given`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "$dpmSourceCapturePath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database"
            )

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Single source with proper argument must be given"
            )
        }
    }

    @Disabled
    @Test
    fun `Should fail when source capture folder is empty`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-folder",
            "${tempFolder.path()}"
        )

        executeCliAndExpectFail(args) { outText, errText ->
            assertThat(outText).isBlank()
            assertThat(errText).isBlank()
        }
    }

    @Test
    fun `Should produce database from DPM source config`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$dpmSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "RDS to DPM mapper",
                "DPM source: Reference Data service",
                "Configuration file: (dm_integration_fixture.json)",
                "Configuration file: OK",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(targetDbPath).exists().isRegularFile()

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics [b1, b4, d10, d16, e3, i12, i6, l11, m7, p8, s9, sd14, si13]",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [DIM, EDA-D1, EDA-D10, EDA-D2, MET]",
                "#TypDims [TDB-D1, TDB-D2]"
            )
        }
    }

    @Test
    fun `Should report error when DPM source config has Metrics without referenced ExpDoms`() {
        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With Metrics",
            retainedElementSources = listOf("metrics")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "DPM dictionary #0: ERROR: DpmDictionary () => metrics",
                "refers non existing ExplicitDomain 'EDA'",
                "Writing dictionaries to DPM database: INFO: Mapping failed due content errors"
            )
        }
    }

    @Test
    fun `Should produce database from DPM source config having Metrics and ExpDoms`() {
        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With Metrics and ExpDoms",
            retainedElementSources = listOf("metrics", "explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture / Partial With Metrics and ExpDoms"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics [b1, b4, d10, d16, e3, i12, i6, l11, m7, p8, s9, sd14, si13]",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should produce database from DPM source config having ExpDoms only`() {

        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With ExpDoms",
            retainedElementSources = listOf("explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture / Partial With ExpDoms"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should produce database from DPM source config having TypDoms only`() {

        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With TypDoms",
            retainedElementSources = listOf("typedDomains")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture / Partial With TypDoms"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should produce database from DPM source config having ExpDims and ExpDoms`() {

        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With ExpDims and ExpDoms",
            retainedElementSources = listOf("explicitDimensions", "explicitDomainsAndHierarchies")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture / Partial With ExpDims and ExpDoms"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, DOME, EDA, EDA1, EDA10, EDA11, EDA20, EDA9, MET]",
                "#TypDoms []",
                "#ExpDims [DIM, EDA-D1, EDA-D10, EDA-D2, MET]",
                "#TypDims []"
            )
        }
    }

    @Test
    fun `Should produce database from DPM source config having TypDims and TypDoms`() {

        val partialSourceConfigPath = createPartialSourceConfig(
            nameTag = "Partial With TypDims and TypDoms",
            retainedElementSources = listOf("typedDimensions", "typedDomains")
        )

        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "$partialSourceConfigPath"
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Configuration file: (partial_source_config.json)",
                "Writing dictionaries to DPM database: OK"
            )

            assertThat(fetchDpmOwnersFromTargetDb()).containsExactlyInAnyOrder(
                "#OwnerNameInDB",
                "EuroFiling",
                "DM Integration Fixture / Partial With TypDims and TypDoms"
            )

            assertThat(fetchElementCodesFromTargetDb()).containsExactly(
                "#Metrics []",
                "#ExpDoms [9999, MET]",
                "#TypDoms [DOMT, TDB, TDD, TDI, TDM, TDP, TDR, TDS]",
                "#ExpDims [MET]",
                "#TypDims [TDB-D1, TDB-D2]"
            )
        }
    }

    @Test
    fun `Should report error when source config file is broken JSON`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_source_config_json.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "Writing dictionaries to DPM database",
                "DPM source: Reference Data service",
                "Configuration file: (broken_source_config_json.json)",
                "FATAL: Processing JSON content failed: "
            )
        }
    }

    @Test
    fun `Should fail when source config file does not exist`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            "${tempFolder.resolve("non_existing_config.json")}"
        )

        executeCliAndExpectFail(args) { outText, errText ->

            assertThat(errText).containsSubsequence(
                "yti-taxgen:",
                "Option source-config: File", "does not exist"
            )

            assertThat(outText).isBlank()
        }
    }

    @Test
    fun `Should report error when source config links to non existing DPM code list`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unknown_codelist.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unknown_codelist",
                "Codelist",
                "Content URLs",
                "FATAL: JSON content fetch failed: HTTP 404 (Not Found)"
            )
        }
    }

    @Test
    fun `Should report error when source config links to unresolvable DPM host name`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_unresolvable_host.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_unresolvable_host",
                "Codelist",
                "Content URLs",
                "FATAL: Could not determine the server IP address"
            )
        }
    }

    @Test
    fun `Should report error when source config has URI with bad protocol`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_bad_protocol.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_bad_protocol",
                "Codelist",
                "Content URLs",
                "FATAL: Malformed URI"
            )
        }
    }

    @Test
    fun `Should report error when source config URI points to non-responsive host IP`() {
        val args = arrayOf(
            "--create-dictionary-to-new-dpm-db",
            "$targetDbPath",
            "--source-config",
            tempTestFixture(RDS_SOURCE_CONFIG, "broken_metric_uri_non_responsive_host_ip.json")
        )

        executeCliAndExpectSuccess(args) { outText ->

            assertThat(outText).containsSubsequence(
                "DPM dictionary", "codelist_uri_non_responsive_host_ip",
                "Codelist",
                "Content URLs",
                "FATAL: Could not connect the server"
            )
        }
    }

    private fun fetchDpmOwnersFromTargetDb(): List<String> {
        val dbConnection = DriverManager.getConnection("jdbc:sqlite:$targetDbPath")
        val rows = dbConnection.createStatement().executeQuery(
            """
                SELECT
                    mOwner.OwnerName AS 'OwnerNameInDB'
                FROM mOwner
                ORDER BY mOwner.OwnerName DESC
            """
        ).toStringList()

        return rows
    }

    private fun fetchElementCodesFromTargetDb(): List<String> {
        val labels = mutableListOf<String>()

        fun populateElementCodes(elementKind: String, query: String) {
            val dbConnection = DriverManager.getConnection("jdbc:sqlite:$targetDbPath")
            val elementLabels = dbConnection.createStatement().executeQuery(query).toStringList(false)

            labels.add("#$elementKind $elementLabels")
        }

        populateElementCodes(
            "Metrics",
            """
            SELECT mMember.MemberCode
            FROM mMetric
            INNER JOIN mMember on mMetric.CorrespondingMemberID = mMember.MemberID
            ORDER BY mMember.MemberCode ASC
            """
        )

        populateElementCodes(
            "ExpDoms",
            """
            SELECT mDomain.DomainCode
            FROM mDomain
            WHERE mDomain.IsTypedDomain = 0
            ORDER BY mDomain.DomainCode ASC
            """
        )

        populateElementCodes(
            "TypDoms",
            """
            SELECT mDomain.DomainCode
            FROM mDomain
            WHERE mDomain.IsTypedDomain = 1
            ORDER BY mDomain.DomainCode ASC
            """
        )

        populateElementCodes(
            "ExpDims",
            """
            SELECT mDimension.DimensionCode
            FROM mDimension
            WHERE mDimension.IsTypedDimension = 0
            ORDER BY mDimension.DimensionCode ASC
            """
        )

        populateElementCodes(
            "TypDims",
            """
            SELECT mDimension.DimensionCode
            FROM mDimension
            WHERE mDimension.IsTypedDimension = 1
            ORDER BY mDimension.DimensionCode ASC
            """
        )

        return labels
    }

    private fun createPartialSourceConfig(
        nameTag: String,
        retainedElementSources: List<String>
    ): Path {
        val mapper = jacksonObjectMapper()

        val configReader = Files.newBufferedReader(Paths.get(dpmSourceConfigPath))
        val config = mapper.readTree(configReader.readText())

        val ownerNode = config.at("/dpmDictionaries/0/owner") as ObjectNode
        val originalName = ownerNode.at("/name").textValue()
        ownerNode.put("name", "$originalName / $nameTag")

        val elementSources = listOf(
            "metrics",
            "explicitDomainsAndHierarchies",
            "explicitDimensions",
            "typedDomains",
            "typedDimensions"
        )

        val discardedElementSources = elementSources - retainedElementSources

        discardedElementSources.forEach { elementSource ->
            val elementSourceNode = config.at("/dpmDictionaries/0/$elementSource") as ObjectNode
            elementSourceNode.putNull("uri")
        }

        val partialConfigContent = mapper.writeValueAsString(config)

        val partialSourceConfigPath =
            tempFolder.createFileWithContent("partial_source_config.json", partialConfigContent)

        return partialSourceConfigPath
    }
}
