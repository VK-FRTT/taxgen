package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
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
            namespace = "FixNSpace",
            prefix = "FixPrfx",
            location = "FixLoc",
            copyright = "FixCop",
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
                        Pair(language("fi"), "$name-Lbl-Fi")
                    ).toMap()
                } else {
                    listOf(
                        Pair(language("fi"), "$name-Lbl-Fi"),
                        Pair(language("en"), "$name-Lbl-En")
                    ).toMap()
                }
            ),
            description = TranslatedText(
                translations = if (variety == FixtureVariety.NO_EN_TRANSLATIONS) {
                    listOf(
                        Pair(language("fi"), "$name-Desc-Fi")
                    ).toMap()
                } else {
                    listOf(
                        Pair(language("fi"), "$name-Desc-Fi"),
                        Pair(language("en"), "$name-Desc-En")
                    ).toMap()
                }

            ),
            owner = dpmOwner
        )

        fun explicitDomains(): List<ExplicitDomain> {

            val members = listOf(
                Member(
                    uri = "Mbr-1-Uri",
                    concept = concept("Mbr-1"),
                    memberCode = "Mbr-1-Code",
                    defaultMember = true
                ),
                Member(
                    uri = "Mbr-2-Uri",
                    concept = concept("Mbr-2"),
                    memberCode = "Mbr-2-Code",
                    defaultMember = false
                ),
                Member(
                    uri = "Mbr-3-Uri",
                    concept = concept("Mbr-3"),
                    memberCode = "Mbr-3-Code",
                    defaultMember = false
                ),
                Member(
                    uri = "Mbr-4-Uri",
                    concept = concept("Mbr-4"),
                    memberCode = "Mbr-4-Code",
                    defaultMember = false
                ),
                Member(
                    uri = "Mbr-5-Uri",
                    concept = concept("Mbr-5"),
                    memberCode = "Mbr-5-Code",
                    defaultMember = false
                )
            )

            val hierarchyNodes = mutableListOf(
                HierarchyNode(
                    uri = "HierNode-1-Uri",
                    concept = concept("HierNode-1"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Mbr-1-Uri",
                    childNodes = emptyList()
                ),

                HierarchyNode(
                    uri = "HierNode-2-Uri",
                    concept = concept("HierNode-2"),
                    abstract = false,
                    comparisonOperator = "=",
                    unaryOperator = "+",
                    referencedMemberUri = "Mbr-2-Uri",
                    childNodes = listOf(
                        HierarchyNode(
                            uri = "HierNode-2.1-Uri",
                            concept = concept("HierNode-2.1"),
                            abstract = false,
                            comparisonOperator = "=",
                            unaryOperator = "+",
                            referencedMemberUri = "Mbr-3-Uri",
                            childNodes = listOf(
                                HierarchyNode(
                                    uri = "HierNode-2.1.1-Uri",
                                    concept = concept("HierNode-2.1.1"),
                                    abstract = false,
                                    comparisonOperator = null,
                                    unaryOperator = null,
                                    referencedMemberUri = "Mbr-4-Uri",
                                    childNodes = emptyList()
                                )
                            )
                        ),

                        HierarchyNode(
                            uri = "HierNode-2.2-Uri",
                            concept = concept("HierNode-2.2"),
                            abstract = false,
                            comparisonOperator = null,
                            unaryOperator = null,
                            referencedMemberUri = "Mbr-5-Uri",
                            childNodes = emptyList()
                        )
                    )
                )
            )

            if (variety == FixtureVariety.SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER) {
                hierarchyNodes.add(
                    HierarchyNode(
                        uri = "HierNode-SecondReferringSameMember-Uri",
                        concept = concept("HierNode-SecondReferringSameMember"),
                        abstract = false,
                        comparisonOperator = null,
                        unaryOperator = null,
                        referencedMemberUri = "Mbr-1-Uri",
                        childNodes = emptyList()
                    )
                )
            }

            val hierarchies = listOf(
                Hierarchy(
                    uri = "Hier-1-Uri",
                    concept = concept("Hierarchy"),
                    hierarchyCode = "Hier-Code",
                    rootNodes = hierarchyNodes
                )
            )

            return listOf(
                ExplicitDomain(
                    uri = "ExpDom-1-Uri",
                    concept = concept("ExplicitDomain"),
                    domainCode = "ExpDom-Code",
                    members = members,
                    hierarchies = hierarchies
                )
            )
        }

        fun typedDomains(): List<TypedDomain> {
            return listOf(
                TypedDomain(
                    uri = "TypDom-1-Uri",
                    concept = concept("TypedDomain"),
                    domainCode = "TypDom-Code",
                    dataType = "Boolean"
                )
            )
        }

        fun explicitDimensions(): List<ExplicitDimension> {
            return emptyList()
        }

        fun typedDimensions(): List<TypedDimension> {
            return emptyList()
        }

        fun metricDomains(): List<MetricDomain> {
            return emptyList()
        }

        return listOf(
            DpmDictionary(
                owner = dpmOwner,
                explicitDomains = explicitDomains(),
                typedDomains = typedDomains(),
                explicitDimensions = explicitDimensions(),
                typedDimensions = typedDimensions(),
                metricDomains = metricDomains()
            )
        )
    }
}
