package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticBridge
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.testcommons.DiagnosticCollectorSimple
import fi.vm.yti.taxgen.testcommons.TempFolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import java.sql.Connection
import java.sql.DriverManager
import java.time.Instant
import java.time.LocalDate

internal open class DpmDbWriter_UnitTestBase {
    protected lateinit var tempFolder: TempFolder
    protected lateinit var diagnosticCollector: DiagnosticCollectorSimple
    protected lateinit var dbConnection: Connection

    fun runDictionaryCreateDbWriter(fixtureVariety: FixtureVariety = FixtureVariety.NONE) {
        tempFolder = TempFolder("sqliteprovider")
        val dbPath = tempFolder.resolve("dpm.db")

        diagnosticCollector = DiagnosticCollectorSimple()
        val diagnosticContext = DiagnosticBridge(diagnosticCollector)

        val dbWriter = DpmDbWriterFactory.dictionaryCreateWriter(
            dbPath,
            false,
            diagnosticContext
        )

        val dictionaries = dpmDictionaryFixture(fixtureVariety)
        dbWriter.writeWithDictionaries(dictionaries)

        dbConnection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
    }

    @AfterEach
    fun baseTeardown() {
        tempFolder.close()

        if (::dbConnection.isInitialized) {
            dbConnection.close()
        }
    }

    enum class FixtureVariety {
        NONE,
        SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER,
        NO_EN_TRANSLATIONS
    }

    protected fun dpmDictionaryFixture(variety: FixtureVariety): List<DpmDictionary> {
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

        fun concept(label: String?, description: String?): Concept {

            fun makeTranslations(basename: String?, kind: String): Map<Language, String> =
                when {
                    basename == null ->
                        emptyMap()

                    variety == FixtureVariety.NO_EN_TRANSLATIONS ->
                        listOf(
                            Pair(language("fi"), "$basename-$kind-Fi")
                        ).toMap()

                    else ->
                        listOf(
                            Pair(language("fi"), "$basename-$kind-Fi"),
                            Pair(language("en"), "$basename-$kind-En")
                        ).toMap()
                }

            val labelTranslations = makeTranslations(label, "Lbl")
            val descriptionTranslations = makeTranslations(description, "Desc")

            return Concept(
                createdAt = Instant.parse("2018-09-03T10:12:25.763Z"),
                modifiedAt = Instant.parse("2018-09-03T22:10:36.863Z"),
                applicableFrom = LocalDate.of(2018, 2, 22),
                applicableUntil = LocalDate.of(2018, 5, 15),
                label = TranslatedText(labelTranslations),
                description = TranslatedText(descriptionTranslations),
                owner = dpmOwner
            )
        }

        fun concept(name: String) = concept(name, name)

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
                    uri = "ExpDomHierNode-1-Uri",
                    concept = concept("ExpDomHierNode-1"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Mbr-1-Uri",
                    childNodes = emptyList()
                ),

                HierarchyNode(
                    uri = "ExpDomHierNode-2-Uri",
                    concept = concept("ExpDomHierNode-2"),
                    abstract = false,
                    comparisonOperator = "=",
                    unaryOperator = "+",
                    referencedMemberUri = "Mbr-2-Uri",
                    childNodes = listOf(

                        HierarchyNode(
                            uri = "ExpDomHierNode-2.1-Uri",
                            concept = concept("ExpDomHierNode-2.1"),
                            abstract = false,
                            comparisonOperator = "=",
                            unaryOperator = "+",
                            referencedMemberUri = "Mbr-3-Uri",
                            childNodes = listOf(

                                HierarchyNode(
                                    uri = "ExpDomHierNode-2.1.1-Uri",
                                    concept = concept(null, "ExpDomHierNode-2.1.1"),
                                    abstract = false,
                                    comparisonOperator = null,
                                    unaryOperator = null,
                                    referencedMemberUri = "Mbr-4-Uri",
                                    childNodes = emptyList()
                                )
                            )
                        ),

                        HierarchyNode(
                            uri = "ExpDomHierNode-2.2-Uri",
                            concept = concept("ExpDomHierNode-2.2"),
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
                    uri = "ExpDomHier-1-Uri",
                    concept = concept("ExpDomHier"),
                    hierarchyCode = "ExpDomHier-1-Code",
                    rootNodes = hierarchyNodes
                )
            )

            return listOf(
                ExplicitDomain(
                    uri = "ExpDom-1-Uri",
                    concept = concept("ExpDom"),
                    domainCode = "ExpDom-1-Code",
                    members = members,
                    hierarchies = hierarchies
                )
            )
        }

        fun typedDomains(): List<TypedDomain> {
            return listOf(
                TypedDomain(
                    uri = "TypDom-1-Uri",
                    concept = concept("TypDom"),
                    domainCode = "TypDom-1-Code",
                    dataType = "Boolean"
                )
            )
        }

        fun explicitDimensions(): List<ExplicitDimension> {
            return listOf(
                ExplicitDimension(
                    uri = "ExpDim-1-Uri",
                    concept = concept("ExpDim"),
                    dimensionCode = "ExpDim-1-Code",
                    referencedDomainCode = "ExpDom-1-Code"
                )
            )
        }

        fun typedDimensions(): List<TypedDimension> {
            return listOf(
                TypedDimension(
                    uri = "TypDim-1-Uri",
                    concept = concept("TypDim"),
                    dimensionCode = "TypDim-1-Code",
                    referencedDomainCode = "TypDom-1-Code"
                )
            )
        }

        fun metricDomains(): List<MetricDomain> {
            val metrics = listOf(
                Metric(
                    uri = "Met-1-Uri",
                    concept = concept("Met-1"),
                    metricCode = "ed1",
                    dataType = "Enumeration/Code",
                    flowType = "Flow",
                    balanceType = "Credit",
                    referencedDomainCode = "ExpDom-1-Code",
                    referencedHierarchyCode = "ExpDomHier-1-Code"
                ),
                Metric(
                    uri = "Met-2-Uri",
                    concept = concept("Met-2"),
                    metricCode = "bd2",
                    dataType = "Boolean",
                    flowType = "Flow",
                    balanceType = "Debit",
                    referencedDomainCode = null,
                    referencedHierarchyCode = null
                ),
                Metric(
                    uri = "Met-3-Uri",
                    concept = concept("Met-3"),
                    metricCode = "di3",
                    dataType = "Date",
                    flowType = "Stock",
                    balanceType = null,
                    referencedDomainCode = null,
                    referencedHierarchyCode = null
                ),
                Metric(
                    uri = "Met-4-Uri",
                    concept = concept("Met-4"),
                    metricCode = "ii4",
                    dataType = "Integer",
                    flowType = "Stock",
                    balanceType = null,
                    referencedDomainCode = null,
                    referencedHierarchyCode = null
                ),
                Metric(
                    uri = "Met-5-Uri",
                    concept = concept("Met-5"),
                    metricCode = "p5",
                    dataType = "Percent",
                    flowType = null,
                    balanceType = null,
                    referencedDomainCode = null,
                    referencedHierarchyCode = null
                )
            )

            val metricHierarchyNodes = mutableListOf(
                HierarchyNode(
                    uri = "MetHierNode-1-Uri",
                    concept = concept("MetHierNode-1"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Met-1-Uri",
                    childNodes = emptyList()
                ),

                HierarchyNode(
                    uri = "MetHierNode-2-Uri",
                    concept = concept("MetHierNode-2"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Met-2-Uri",
                    childNodes = listOf(

                        HierarchyNode(
                            uri = "MetHierNode-2.1-Uri",
                            concept = concept("MetHierNode-2.1"),
                            abstract = false,
                            comparisonOperator = null,
                            unaryOperator = null,
                            referencedMemberUri = "Met-3-Uri",
                            childNodes = emptyList()
                        ),

                        HierarchyNode(
                            uri = "MetHierNode-2.2-Uri",
                            concept = concept("MetHierNode-2.2"),
                            abstract = false,
                            comparisonOperator = null,
                            unaryOperator = null,
                            referencedMemberUri = "Met-4-Uri",
                            childNodes = emptyList()
                        )
                    )
                ),

                HierarchyNode(
                    uri = "MetHierNode-3-Uri",
                    concept = concept("MetHierNode-3"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedMemberUri = "Met-5-Uri",
                    childNodes = emptyList()
                )
            )

            val metricHierarchies = listOf(
                Hierarchy(
                    uri = "MetHier-1-Uri",
                    concept = concept("MetHier"),
                    hierarchyCode = "MetHier-1-Code",
                    rootNodes = metricHierarchyNodes
                )
            )

            return listOf(
                MetricDomain(
                    uri = "MetDom-1-Uri",
                    concept = concept("MetDom"),
                    domainCode = "MetDom-1-Code",
                    metrics = metrics,
                    hierarchies = metricHierarchies
                )
            )
        }

        val dictionary = DpmDictionary(
            owner = dpmOwner,
            explicitDomains = explicitDomains(),
            typedDomains = typedDomains(),
            explicitDimensions = explicitDimensions(),
            typedDimensions = typedDimensions(),
            metricDomains = metricDomains()
        )

        if (variety != FixtureVariety.SECOND_HIERARCHY_NODE_REFERS_SAME_MEMBER) {
            validateDictionaryContents(dictionary)
        }

        return listOf(dictionary)
    }

    private fun validateDictionaryContents(dpmDictionary: DpmDictionary) {
        val collecor = ValidationCollector()

        dpmDictionary.explicitDomains.forEach { explicitDomain ->
            explicitDomain.members.forEach { it.validate(collecor) }
            explicitDomain.hierarchies.forEach { it.validate(collecor) }
            explicitDomain.validate(collecor)
        }

        dpmDictionary.typedDomains.forEach { typedDomain ->
            typedDomain.validate(collecor)
        }

        dpmDictionary.explicitDimensions.forEach { explicitDimension ->
            explicitDimension.validate(collecor)
        }

        dpmDictionary.typedDimensions.forEach { typedDimension ->
            typedDimension.validate(collecor)
        }

        dpmDictionary.metricDomains.forEach { metricDomain ->
            metricDomain.metrics.forEach { it.validate(collecor) }
            metricDomain.hierarchies.forEach { it.validate(collecor) }
            metricDomain.validate(collecor)
        }

        dpmDictionary.validate(collecor)

        assertThat(collecor.compileResultsToSimpleStrings()).isEmpty()
    }
}
