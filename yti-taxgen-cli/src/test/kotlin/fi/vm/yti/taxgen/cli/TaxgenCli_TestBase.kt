package fi.vm.yti.taxgen.cli

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.testcommons.TempFolder
import fi.vm.yti.taxgen.testcommons.TestFixture
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.DPM_DB
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_CAPTURE
import fi.vm.yti.taxgen.testcommons.TestFixture.Type.RDS_SOURCE_CONFIG
import fi.vm.yti.taxgen.testcommons.ext.java.toStringList
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.DriverManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

open class TaxgenCli_TestBase(val primaryCommand: String? = null) {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var integrationFixtureCapturePath: String
    protected lateinit var integrationFixtureConfigPath: String

    private lateinit var charset: Charset
    private lateinit var outCollector: PrintStreamCollector
    private lateinit var errCollector: PrintStreamCollector

    private lateinit var cli: TaxgenCli

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("taxgen_cli")

        integrationFixtureCapturePath = cloneTestFixtureToTemp(RDS_CAPTURE, "integration_fixture").toString()
        integrationFixtureConfigPath = cloneTestFixtureToTemp(RDS_SOURCE_CONFIG, "integration_fixture.json").toString()

        charset = StandardCharsets.UTF_8
        outCollector = PrintStreamCollector(charset)
        errCollector = PrintStreamCollector(charset)

        cli = TaxgenCli(
            outStream = outCollector.printStream(),
            errStream = errCollector.printStream(),
            charset = charset,
            definedOptions = DefinedOptions()
        )
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()
    }

    protected fun cloneTestFixtureToTemp(
        fixtureType: TestFixture.Type,
        fixtureName: String
    ): Path {

        val fixtureSourcePath = TestFixture.sourcePathOf(fixtureType, fixtureName)

        return when (fixtureType) {

            RDS_CAPTURE -> tempFolder.copyFolderRecursivelyUnderSubfolder(
                fixtureSourcePath,
                "${fixtureType.folderName}_$fixtureName"
            )

            RDS_SOURCE_CONFIG -> tempFolder.copyFileToSubfolder(
                fixtureSourcePath,
                fixtureType.folderName
            )

            DPM_DB -> tempFolder.copyFileToSubfolder(
                fixtureSourcePath,
                fixtureType.folderName
            )

            else -> thisShouldNeverHappen("Unsupported fixture type")
        }
    }

    private fun executeCli(args: Array<String>): ExecuteResult {
        if (primaryCommand != null) {
            assertThat(args).contains(primaryCommand)
        }

        val status = cli.execute(args)

        val result = ExecuteResult(
            status,
            outCollector.grabText(),
            errCollector.grabText()
        )

        // println("OUT >>>\n${result.outText}\n<<< OUT")
        // println("ERR >>>\n${result.errText}\n<<< ERR")

        return result
    }

    protected fun executeCliAndExpectSuccess(args: Array<String>, verifier: (String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isBlank()

        verifier(result.outText)

        assertThat(result.status).isEqualTo(TAXGEN_CLI_SUCCESS)
    }

    protected fun executeCliAndExpectFail(args: Array<String>, verifier: (String, String) -> Unit) {
        val result = executeCli(args)

        assertThat(result.errText).isNotBlank()

        verifier(result.outText, result.errText)

        assertThat(result.status).isEqualTo(TAXGEN_CLI_FAIL)
    }

    private class PrintStreamCollector(val charset: Charset) {
        private val baos = ByteArrayOutputStream()
        private val ps = PrintStream(baos, true, charset.name())

        fun printStream(): PrintStream = ps

        fun grabText(): String {
            ps.close()
            return String(baos.toByteArray(), charset)
        }
    }

    private data class ExecuteResult(
        val status: Int,
        val outText: String,
        val errText: String
    )

    fun updateDb(
        dbPath: Path,
        sql: String
    ) {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath").use { dbConnection ->
            dbConnection.createStatement().executeUpdate(sql)
        }
    }

    fun fetchDpmOwnersFromDb(dbPath: Path): List<String> {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath").use { dbConnection ->
            dbConnection.createStatement().executeQuery(
                """
                SELECT
                    mOwner.OwnerName AS 'OwnerNameInDB'
                FROM mOwner
                ORDER BY mOwner.OwnerName DESC
            """
            ).toStringList()
        }
    }

    fun fetchElementCodesFromDb(dbPath: Path): List<String> {

        return DriverManager.getConnection("jdbc:sqlite:$dbPath").use { dbConnection ->

            val labels = mutableListOf<String>()

            fun populateElementCodes(
                elementKind: String,
                query: String
            ) {
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

            labels
        }
    }

    fun clonePartialSourceConfigFromConfig(
        configPath: String,
        nameTag: String,
        retainedElementSources: List<String>
    ): Path {
        val mapper = jacksonObjectMapper()

        val configReader = Files.newBufferedReader(Paths.get(configPath))
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
