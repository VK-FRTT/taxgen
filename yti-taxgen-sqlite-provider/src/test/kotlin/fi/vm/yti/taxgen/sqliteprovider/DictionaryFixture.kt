package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.commons.datavalidation.ValidationCollector
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.DpmModelOption
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
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.time.LocalDate

enum class FixtureVariety {
    NONE,
    TWO_HIERARCHY_NODES_REFER_SAME_MEMBER,
    THREE_EXPLICIT_DOMAINS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES,
    THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES,
    TRANSLATIONS_FI_ONLY,
    TRANSLATIONS_FI_SV
}

fun dpmModelFixture(
    variety: FixtureVariety,
    modelOptions: Map<DpmModelOption, Any>
): DpmModel {
    fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    val dpmOwner = Owner(
        name = "FixName",
        namespace = "FixNSpace",
        prefix = "FixPrfx",
        location = "FixLoc",
        copyright = "FixCop",
        languageCodes = listOf("en", "fi", "sv"),
        defaultLanguageCode = "fi"
    )

    fun concept(label: String?, description: String?): Concept {

        fun makeTranslations(basename: String?, kind: String): Map<Language, String> =
            when {
                basename == null ->
                    emptyMap()

                variety == FixtureVariety.TRANSLATIONS_FI_ONLY ->
                    listOf(
                        Pair(language("fi"), "$basename-$kind-Fi")
                    ).toMap()

                variety == FixtureVariety.TRANSLATIONS_FI_SV ->
                    listOf(
                        Pair(language("fi"), "$basename-$kind-Fi"),
                        Pair(language("sv"), "$basename-$kind-Sv")
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
            ),
            Member(
                uri = "Mbr-6-Uri",
                concept = concept("Mbr-6"),
                memberCode = "Mbr-6-Code",
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
                referencedElementCode = "Mbr-1-Code",
                childNodes = emptyList()
            ),

            HierarchyNode(
                uri = "ExpDomHierNode-2-Uri",
                concept = concept("ExpDomHierNode-2"),
                abstract = false,
                comparisonOperator = "=",
                unaryOperator = "+",
                referencedElementCode = "Mbr-2-Code",
                childNodes = listOf(

                    HierarchyNode(
                        uri = "ExpDomHierNode-2.1-Uri",
                        concept = concept("ExpDomHierNode-2.1"),
                        abstract = false,
                        comparisonOperator = "=",
                        unaryOperator = "+",
                        referencedElementCode = "Mbr-3-Code",
                        childNodes = listOf(

                            HierarchyNode(
                                uri = "ExpDomHierNode-2.1.1-Uri",
                                concept = concept(null, "ExpDomHierNode-2.1.1"),
                                abstract = false,
                                comparisonOperator = null,
                                unaryOperator = null,
                                referencedElementCode = "Mbr-4-Code",
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
                        referencedElementCode = "Mbr-5-Code",
                        childNodes = emptyList()
                    )
                )
            )
        )

        if (variety == FixtureVariety.TWO_HIERARCHY_NODES_REFER_SAME_MEMBER) {
            hierarchyNodes.add(
                HierarchyNode(
                    uri = "HierNode-SecondReferringSameMember-Uri",
                    concept = concept("HierNode-SecondReferringSameMember"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedElementCode = "Mbr-1-Code",
                    childNodes = emptyList()
                )
            )
        }

        val hierarchies = listOf(
            Hierarchy(
                uri = "ExpDomHier-1-Uri",
                concept = concept("ExpDomHier"),
                hierarchyCode = "ExpDomHier-1-Code",
                rootNodes = emptyList()
            ),
            Hierarchy(
                uri = "ExpDomHier-2-Uri",
                concept = concept("ExpDomHier"),
                hierarchyCode = "ExpDomHier-2-Code",
                rootNodes = hierarchyNodes
            ),
            Hierarchy(
                uri = "ExpDomHier-3-Uri",
                concept = concept("ExpDomHier"),
                hierarchyCode = "ExpDomHier-3-Code",
                rootNodes = emptyList()
            )
        )

        val explicitDomains = mutableListOf(
            ExplicitDomain(
                uri = "ExpDom-1-Uri",
                concept = concept("ExpDom"),
                domainCode = "ExpDom-1-Code",
                members = members,
                hierarchies = hierarchies
            )
        )

        if (variety == FixtureVariety.THREE_EXPLICIT_DOMAINS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES ||
            variety == FixtureVariety.THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES
        ) {
            explicitDomains.add(
                ExplicitDomain(
                    uri = "ExpDom-2-Uri",
                    concept = concept("ExpDom"),
                    domainCode = "ExpDom-2-Code",
                    members = members,
                    hierarchies = hierarchies
                )
            )

            explicitDomains.add(
                ExplicitDomain(
                    uri = "ExpDom-3-Uri",
                    concept = concept("ExpDom"),
                    domainCode = "ExpDom-3-Code",
                    members = members,
                    hierarchies = hierarchies
                )
            )
        }

        return explicitDomains
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
        val explicitDimensions = mutableListOf(
            ExplicitDimension(
                uri = "ExpDim-1-Uri",
                concept = concept("ExpDim"),
                dimensionCode = "ExpDim-1-Code",
                referencedDomainCode = "ExpDom-1-Code"
            )
        )

        if (variety == FixtureVariety.THREE_EXPLICIT_DIMENSIONS_WITH_EQUALLY_IDENTIFIED_MEMBERS_AND_HIERARCHIES) {
            explicitDimensions.add(
                ExplicitDimension(
                    uri = "ExpDim-2-Uri",
                    concept = concept("ExpDim"),
                    dimensionCode = "ExpDim-2-Code",
                    referencedDomainCode = "ExpDom-2-Code"
                )
            )

            explicitDimensions.add(
                ExplicitDimension(
                    uri = "ExpDim-3-Uri",
                    concept = concept("ExpDim"),
                    dimensionCode = "ExpDim-3-Code",
                    referencedDomainCode = "ExpDom-3-Code"
                )
            )
        }

        return explicitDimensions
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
                referencedElementCode = "ed1",
                childNodes = emptyList()
            ),

            HierarchyNode(
                uri = "MetHierNode-2-Uri",
                concept = concept("MetHierNode-2"),
                abstract = false,
                comparisonOperator = null,
                unaryOperator = null,
                referencedElementCode = "bd2",
                childNodes = listOf(

                    HierarchyNode(
                        uri = "MetHierNode-2.1-Uri",
                        concept = concept("MetHierNode-2.1"),
                        abstract = false,
                        comparisonOperator = null,
                        unaryOperator = null,
                        referencedElementCode = "di3",
                        childNodes = emptyList()
                    ),

                    HierarchyNode(
                        uri = "MetHierNode-2.2-Uri",
                        concept = concept("MetHierNode-2.2"),
                        abstract = false,
                        comparisonOperator = null,
                        unaryOperator = null,
                        referencedElementCode = "ii4",
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
                referencedElementCode = "p5",
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

    val model = DpmModel(
        dictionaries = listOf(dictionary),
        modelOptions = modelOptions
    )

    if (variety != FixtureVariety.TWO_HIERARCHY_NODES_REFER_SAME_MEMBER) {
        validateModelContents(model)
    }

    return model
}

private fun validateModelContents(dpmModel: DpmModel) {
    val collecor = ValidationCollector()

    dpmModel.dictionaries.forEach { dictionary ->

        dictionary.explicitDomains.forEach { explicitDomain ->
            explicitDomain.members.forEach { it.validate(collecor) }
            explicitDomain.hierarchies.forEach { it.validate(collecor) }
            explicitDomain.validate(collecor)
        }

        dictionary.typedDomains.forEach { typedDomain ->
            typedDomain.validate(collecor)
        }

        dictionary.explicitDimensions.forEach { explicitDimension ->
            explicitDimension.validate(collecor)
        }

        dictionary.typedDimensions.forEach { typedDimension ->
            typedDimension.validate(collecor)
        }

        dictionary.metricDomains.forEach { metricDomain ->
            metricDomain.metrics.forEach { it.validate(collecor) }
            metricDomain.hierarchies.forEach { it.validate(collecor) }
            metricDomain.validate(collecor)
        }

        dictionary.validate(collecor)
    }

    dpmModel.validate(collecor)

    //NOTE: When this assert triggers, it means that most likely something
    //is broken within this test fixture internal relations
    assertThat(collecor.compileResultsToSimpleStrings()).isEmpty()
}
