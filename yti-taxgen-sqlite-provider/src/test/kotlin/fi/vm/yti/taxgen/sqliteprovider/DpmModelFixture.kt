package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmModel
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
import fi.vm.yti.taxgen.dpmmodel.datavalidation.system.ValidationCollector
import java.time.Instant
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat

enum class FixtureVariety {
    NONE,
    TWO_HIERARCHY_NODES_REFER_SAME_MEMBER,
    TRANSLATIONS_FI_ONLY,
    TRANSLATIONS_FI_SV,
    TRANSLATIONS_DROP_HIERARCHY_NODE_FI_LABEL,
    ONLY_FIRST_EXPLICIT_DOMAIN,
    ONLY_FIRST_EXPLICIT_DIMENSION,
    ONLY_FIRST_EXPLICIT_DOMAIN_MEMBER,
    ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY,
    ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY_NODE,
    ONLY_ONE_DICTIONARY,
    NO_METRIC_DOMAIN,
}

fun dpmDictionary(
    varieties: List<FixtureVariety>,
    dictionaryScopePrefix: String = ""
): DpmDictionary {

    fun language(languageCode: String) = Language.findByIso6391Code(languageCode)!!

    val dpmOwner = Owner(
        name = "${dictionaryScopePrefix}FixName",
        namespace = "FixNSpace",
        prefix = "${dictionaryScopePrefix}FixPrfx",
        location = "FixLoc",
        copyright = "FixCop",
        languageCodes = listOf("en", "fi", "sv")
    )

    fun concept(conceptBasename: String): Concept {

        fun makeTranslations(basename: String, kind: String): Map<Language, String> =
            when {
                varieties.contains(FixtureVariety.TRANSLATIONS_FI_ONLY) ->
                    listOf(
                        Pair(language("fi"), "$dictionaryScopePrefix$basename-$kind-Fi")
                    ).toMap()

                varieties.contains(FixtureVariety.TRANSLATIONS_FI_SV) ->
                    listOf(
                        Pair(language("fi"), "$dictionaryScopePrefix$basename-$kind-Fi"),
                        Pair(language("sv"), "$dictionaryScopePrefix$basename-$kind-Sv")
                    ).toMap()

                else ->
                    listOf(
                        Pair(language("fi"), "$dictionaryScopePrefix$basename-$kind-Fi"),
                        Pair(language("en"), "$dictionaryScopePrefix$basename-$kind-En")
                    ).toMap()
            }

        val labelTranslations = makeTranslations(conceptBasename, "Lbl")
        val descriptionTranslations = makeTranslations(conceptBasename, "Desc")

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

    fun explicitDomains(): List<ExplicitDomain> {

        fun members(): List<Member> {

            val members = mutableListOf(
                Member(
                    uri = "Mbr-1-Uri",
                    concept = concept("Mbr-1"),
                    memberCode = "Mbr-1-Code",
                    defaultMember = true
                )
            )

            if (FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_MEMBER !in varieties) {
                members.add(
                    Member(
                        uri = "Mbr-2-Uri",
                        concept = concept("Mbr-2"),
                        memberCode = "Mbr-2-Code",
                        defaultMember = false
                    )
                )

                members.add(
                    Member(
                        uri = "Mbr-3-Uri",
                        concept = concept("Mbr-3"),
                        memberCode = "Mbr-3-Code",
                        defaultMember = false
                    )
                )

                members.add(
                    Member(
                        uri = "Mbr-4-Uri",
                        concept = concept("Mbr-4"),
                        memberCode = "Mbr-4-Code",
                        defaultMember = false
                    )
                )
                members.add(
                    Member(
                        uri = "Mbr-5-Uri",
                        concept = concept("Mbr-5"),
                        memberCode = "Mbr-5-Code",
                        defaultMember = false
                    )
                )

                members.add(
                    Member(
                        uri = "Mbr-6-Uri",
                        concept = concept("Mbr-6"),
                        memberCode = "Mbr-6-Code",
                        defaultMember = false
                    )
                )
            }

            return members
        }

        fun hierarchyNodes(): List<HierarchyNode> {

            val hierarchyNodes = mutableListOf(

                HierarchyNode(
                    uri = "ExpDomHierNode-1-Uri",
                    concept = concept("ExpDomHierNode-1"),
                    abstract = false,
                    comparisonOperator = null,
                    unaryOperator = null,
                    referencedElementCode = "Mbr-1-Code",
                    childNodes = emptyList()
                )
            )

            if (FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_MEMBER !in varieties &&
                FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY_NODE !in varieties
            ) {
                hierarchyNodes.add(

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
                                        concept = concept("ExpDomHierNode-2.1.1").let {
                                            if (FixtureVariety.TRANSLATIONS_DROP_HIERARCHY_NODE_FI_LABEL in varieties) {
                                                val translations = it.label.translations.toMutableMap()
                                                translations.remove(Language.byIso6391CodeOrFail("fi"))
                                                it.copy(
                                                    label = TranslatedText(translations)
                                                )
                                            } else {
                                                it
                                            }
                                        },
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
            }

            if (FixtureVariety.TWO_HIERARCHY_NODES_REFER_SAME_MEMBER in varieties) {
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

            return hierarchyNodes
        }

        fun hierarchies(): List<Hierarchy> {

            val hierarchies = mutableListOf(
                Hierarchy(
                    uri = "ExpDomHier-1-Uri",
                    concept = concept("ExpDomHier-1"),
                    hierarchyCode = "ExpDomHier-1-Code",
                    rootNodes = emptyList()
                )
            )

            if (FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN_HIERARCHY !in varieties) {
                hierarchies.addAll(
                    listOf(
                        Hierarchy(
                            uri = "ExpDomHier-2-Uri",
                            concept = concept("ExpDomHier-2"),
                            hierarchyCode = "ExpDomHier-2-Code",
                            rootNodes = hierarchyNodes()
                        ),

                        Hierarchy(
                            uri = "ExpDomHier-3-Uri",
                            concept = concept("ExpDomHier-3"),
                            hierarchyCode = "ExpDomHier-3-Code",
                            rootNodes = emptyList()
                        )
                    )
                )
            }

            return hierarchies
        }

        val members = members()
        val hierarchies = hierarchies()

        fun explicitDomain(expDomBasename: String) = ExplicitDomain(
            uri = "$expDomBasename-Uri",
            concept = concept(expDomBasename),
            domainCode = "$expDomBasename-Code",
            members = members,
            hierarchies = hierarchies
        )

        val explicitDomains = mutableListOf(
            explicitDomain(
                expDomBasename = "${dictionaryScopePrefix}ExpDom-1"
            )
        )

        if (FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN !in varieties) {

            explicitDomains.add(
                explicitDomain(
                    expDomBasename = "${dictionaryScopePrefix}ExpDom-2"
                )
            )

            explicitDomains.add(
                explicitDomain(
                    expDomBasename = "${dictionaryScopePrefix}ExpDom-3"
                )
            )
        }

        return explicitDomains
    }

    fun typedDomains(): List<TypedDomain> {
        return listOf(
            TypedDomain(
                uri = "${dictionaryScopePrefix}TypDom-1-Uri",
                concept = concept("${dictionaryScopePrefix}TypDom-1"),
                domainCode = "${dictionaryScopePrefix}TypDom-1-Code",
                dataType = "Boolean"
            )
        )
    }

    fun explicitDimensions(): List<ExplicitDimension> {
        val explicitDimensions = mutableListOf(
            ExplicitDimension(
                uri = "ExpDim-1-Uri",
                concept = concept("ExpDim-1"),
                dimensionCode = "ExpDim-1-Code",
                referencedDomainCode = "${dictionaryScopePrefix}ExpDom-1-Code"
            )
        )

        if (FixtureVariety.ONLY_FIRST_EXPLICIT_DOMAIN !in varieties &&
            FixtureVariety.ONLY_FIRST_EXPLICIT_DIMENSION !in varieties
        ) {
            explicitDimensions.add(
                ExplicitDimension(
                    uri = "ExpDim-2-Uri",
                    concept = concept("ExpDim-2"),
                    dimensionCode = "ExpDim-2-Code",
                    referencedDomainCode = "${dictionaryScopePrefix}ExpDom-2-Code"
                )
            )

            explicitDimensions.add(
                ExplicitDimension(
                    uri = "ExpDim-3-Uri",
                    concept = concept("ExpDim-3"),
                    dimensionCode = "ExpDim-3-Code",
                    referencedDomainCode = "${dictionaryScopePrefix}ExpDom-3-Code"
                )
            )
        }

        return explicitDimensions
    }

    fun typedDimensions(): List<TypedDimension> {
        return listOf(
            TypedDimension(
                uri = "TypDim-1-Uri",
                concept = concept("TypDim-1"),
                dimensionCode = "TypDim-1-Code",
                referencedDomainCode = "${dictionaryScopePrefix}TypDom-1-Code"
            )
        )
    }

    fun metricDomain(): MetricDomain? {
        if (FixtureVariety.NO_METRIC_DOMAIN in varieties) {
            return null
        }

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
                concept = concept("MetHier-1"),
                hierarchyCode = "MetHier-1-Code",
                rootNodes = metricHierarchyNodes
            )
        )

        return MetricDomain(
            uri = "MetDom-1-Uri",
            concept = concept("MetDom-1"),
            domainCode = "MetDom-1-Code",
            metrics = metrics,
            hierarchies = metricHierarchies
        )
    }

    return DpmDictionary(
        owner = dpmOwner,
        explicitDomains = explicitDomains(),
        typedDomains = typedDomains(),
        explicitDimensions = explicitDimensions(),
        typedDimensions = typedDimensions(),
        metricDomain = metricDomain()
    )
}

fun dpmModelFixture(
    vararg varieties: FixtureVariety
): DpmModel {
    val varietiesList =
        varieties.toList()

    val dictionaries = if (FixtureVariety.ONLY_ONE_DICTIONARY in varieties) {
        listOf(
            dpmDictionary(varietiesList)
        )
    } else {
        listOf(
            dpmDictionary(varietiesList + FixtureVariety.NO_METRIC_DOMAIN, "A"),
            dpmDictionary(varietiesList),
            dpmDictionary(varietiesList + FixtureVariety.NO_METRIC_DOMAIN, "C")
        )
    }

    val model = DpmModel(
        dictionaries = dictionaries
    )

    if (FixtureVariety.TWO_HIERARCHY_NODES_REFER_SAME_MEMBER !in varieties) {
        validateModelContents(model)
    }

    return model
}

private fun validateModelContents(dpmModel: DpmModel) {
    val collector = ValidationCollector()

    dpmModel.dictionaries.forEach { dictionary ->

        dictionary.explicitDomains.forEach { explicitDomain ->
            explicitDomain.members.forEach { it.validate(collector) }
            explicitDomain.hierarchies.forEach { it.validate(collector) }
            explicitDomain.validate(collector)
        }

        dictionary.typedDomains.forEach { typedDomain ->
            typedDomain.validate(collector)
        }

        dictionary.explicitDimensions.forEach { explicitDimension ->
            explicitDimension.validate(collector)
        }

        dictionary.typedDimensions.forEach { typedDimension ->
            typedDimension.validate(collector)
        }

        dictionary.metricDomain?.let { metricDomain ->
            metricDomain.metrics.forEach { it.validate(collector) }
            metricDomain.hierarchies.forEach { it.validate(collector) }
            metricDomain.validate(collector)
        }

        dictionary.validate(collector)
    }

    dpmModel.validate(collector)

    // NOTE: When this assert triggers, it means that most likely something
    // is broken within this test fixture internal relations
    assertThat(collector.compileResultsToSimpleStrings()).isEmpty()
}
