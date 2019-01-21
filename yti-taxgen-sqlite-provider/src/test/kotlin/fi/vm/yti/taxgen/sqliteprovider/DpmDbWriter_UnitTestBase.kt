package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDate

internal open class DpmDbWriter_UnitTestBase {
    protected lateinit var tempFolder: TempFolder

    protected lateinit var diagnosticCollector: DiagnosticCollectorSimple
    protected lateinit var diagnosticContext: DiagnosticContext

    protected lateinit var dbWriter: DpmDbWriter
    protected lateinit var dbConnection: Connection

    @BeforeEach
    fun baseInit() {
        tempFolder = TempFolder("sqliteprovider")

        val dbPath = tempFolder.resolve("dpm.db")

        diagnosticCollector = DiagnosticCollectorSimple()
        diagnosticContext = DiagnosticBridge(diagnosticCollector)

        dbWriter = DpmDbWriter(
            dbPath,
            false,
            diagnosticContext
        )

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()
        dbConnection.close()
    }

    enum class FixtureVariety {
        NONE,
        SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER,
        NO_EN_TRANSLATIONS
    }

    protected fun dpmDictionaryFixture(variety: FixtureVariety = FixtureVariety.NONE): List<DpmDictionary> {
        fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

        val dpmOwner = Owner(
            name = "FixName",
            namespace = "FixNamespace",
            prefix = "FixPrefix",
            location = "FixLocation",
            copyright = "FixCopyright",
            languageCodes = listOf("en", "fi"),
            defaultLanguageCode = "fi"
        )

        fun concept(name: String) = Concept(
            createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
            modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
            applicableFrom = LocalDate.of(2018, 2, 22),
            applicableUntil = LocalDate.of(2018, 5, 15),
            label = TranslatedText(
                translations = if (variety == FixtureVariety.NO_EN_TRANSLATIONS) {
                    listOf(
                        Pair(language("fi"), "$name-LabelFi")
                    ).toMap()
                } else {
                    listOf(
                        Pair(language("fi"), "$name-LabelFi"),
                        Pair(language("en"), "$name-LabelEn")
                    ).toMap()
                }
            ),
            description = TranslatedText(
                translations = if (variety == FixtureVariety.NO_EN_TRANSLATIONS) {
                    listOf(
                        Pair(language("fi"), "$name-DescriptionFi")
                    ).toMap()
                } else {
                    listOf(
                        Pair(language("fi"), "$name-DescriptionFi"),
                        Pair(language("en"), "$name-DescriptionEn")
                    ).toMap()
                }

            ),
            owner = dpmOwner
        )

        val members = listOf(
            Member(
                uri = "Member-1-Uri",
                concept = concept("Member-1"),
                memberCode = "Member-1-Code",
                defaultMember = true
            ),
            Member(
                uri = "Member-2-Uri",
                concept = concept("Member-2"),
                memberCode = "Member-2-Code",
                defaultMember = false
            ),
            Member(
                uri = "Member-3-Uri",
                concept = concept("Member-3"),
                memberCode = "Member-3-Code",
                defaultMember = false
            ),
            Member(
                uri = "Member-4-Uri",
                concept = concept("Member-4"),
                memberCode = "Member-4-Code",
                defaultMember = false
            ),
            Member(
                uri = "Member-5-Uri",
                concept = concept("Member-5"),
                memberCode = "Member-5-Code",
                defaultMember = false
            )
        )

        val hierarchyNodes = mutableListOf(
            HierarchyNode(
                uri = "HierarchyNode-1-Uri",
                concept = concept("HierarchyNode-1"),
                abstract = false,
                comparisonOperator = null,
                unaryOperator = null,
                referencedMemberUri = "Member-1-Uri",
                childNodes = emptyList()
            ),

            HierarchyNode(
                uri = "HierarchyNode-2-Uri",
                concept = concept("HierarchyNode-2"),
                abstract = false,
                comparisonOperator = "=",
                unaryOperator = "+",
                referencedMemberUri = "Member-2-Uri",
                childNodes = listOf(
                    HierarchyNode(
                        uri = "HierarchyNode-2.1-Uri",
                        concept = concept("HierarchyNode-2.1"),
                        abstract = false,
                        comparisonOperator = "=",
                        unaryOperator = "+",
                        referencedMemberUri = "Member-3-Uri",
                        childNodes = listOf(
                            HierarchyNode(
                                uri = "HierarchyNode-2.1.1-Uri",
                                concept = concept("HierarchyNode-2.1.1"),
                                abstract = false,
                                comparisonOperator = null,
                                unaryOperator = null,
                                referencedMemberUri = "Member-4-Uri",
                                childNodes = emptyList()
                            )
                        )
                    ),

                    HierarchyNode(
                        uri = "HierarchyNode-2.2-Uri",
                        concept = concept("HierarchyNode-2.2"),
                        abstract = false,
                        comparisonOperator = null,
                        unaryOperator = null,
                        referencedMemberUri = "Member-5-Uri",
                        childNodes = emptyList()
                    )
                )
            )
        )

        if (variety == FixtureVariety.SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER) {
            hierarchyNodes.add(
                HierarchyNode(
                    uri = "HierarchyNode-SecondReferringSameMember-Uri",
                    concept = concept("HierarchyNode-§SecondReferringSameMember"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Member-1-Uri",
                    childNodes = emptyList()
                )
            )
        }

        val hierarchies = listOf(
            Hierarchy(
                uri = "Hierarchy-1-Uri",
                concept = concept("Hierarchy"),
                hierarchyCode = "Hierarchy-Code",
                rootNodes = hierarchyNodes
            )
        )

        val dictionaries =
            listOf(
                DpmDictionary(
                    owner = dpmOwner,

                    metrics = emptyList(),

                    explicitDomains = listOf(
                        ExplicitDomain(
                            uri = "ExplicitDomain-1-Uri",
                            concept = concept("ExplicitDomain"),
                            domainCode = "Domain-Code",
                            members = members,
                            hierarchies = hierarchies
                        )
                    ),

                    typedDomains = emptyList(),
                    explicitDimensions = emptyList(),
                    typedDimensions = emptyList()
                )
            )

        return dictionaries
    }
}
