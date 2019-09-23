package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datafactory.DataDefinition
import fi.vm.yti.taxgen.dpmmodel.datafactory.dynamicAttribute
import java.time.Instant
import java.time.LocalDate

fun dpmTestData(): Set<DataDefinition> {
    var definitions = HashSet<DataDefinition>()

    definitions.add(
        DataDefinition(
            kClass = Language::class,
            attributes = mapOf(
                "iso6391Code" to "en",
                "label" to dynamicAttribute { it.instantiate<TranslatedText>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TranslatedText::class,
            attributes = mapOf(
                "translations" to mapOf(
                    Language.findByIso6391Code("en") to "Text#en",
                    Language.findByIso6391Code("fi") to "Text#fi"
                )
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Owner::class,
            attributes = mapOf(
                "name" to "name_value",
                "namespace" to "namespace_value",
                "prefix" to "ns_prefix",
                "location" to "official_location",
                "copyright" to "Lorem ipsum",
                "languageCodes" to listOf("en", "fi")
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Concept::class,
            attributes = mapOf(
                "createdAt" to Instant.parse("2018-03-20T10:20:30.40Z"),
                "modifiedAt" to Instant.parse("2018-03-22T10:20:30.40Z"),
                "applicableFrom" to LocalDate.of(2018, 3, 20),
                "applicableUntil" to LocalDate.of(2018, 4, 20),
                "label" to dynamicAttribute { it.instantiate<TranslatedText>() },
                "description" to dynamicAttribute { it.instantiate<TranslatedText>() },
                "owner" to dynamicAttribute { it.instantiate<Owner>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDomain::class,
            attributes = mapOf(
                "uri" to "exp_dom_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "exp_dom",
                "members" to dynamicAttribute { listOf(it.instantiate<Member>()) },
                "hierarchies" to dynamicAttribute { listOf(it.instantiate<Hierarchy>()) }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Member::class,
            attributes = mapOf(
                "uri" to "mem_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "memberCode" to "mc",
                "defaultMember" to true
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Hierarchy::class,
            attributes = mapOf(
                "uri" to "hie_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "hierarchyCode" to "hier_code",
                "rootNodes" to listOf<HierarchyNode>()
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = HierarchyNode::class,
            attributes = mapOf(
                "uri" to "hie_nod_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "abstract" to false,
                "comparisonOperator" to "=",
                "unaryOperator" to "+",
                "referencedElementCode" to "mem_code",
                "childNodes" to listOf<HierarchyNode>()
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TypedDomain::class,
            attributes = mapOf(
                "uri" to "typ_dom_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "typ_dom",
                "dataType" to "String"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDimension::class,
            attributes = mapOf(
                "uri" to "exp_dim_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "dimensionCode" to "exp_dim",
                "referencedDomainCode" to "exp_dom"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = TypedDimension::class,
            attributes = mapOf(
                "uri" to "typ_dim_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "dimensionCode" to "typ_dim",
                "referencedDomainCode" to "typ_dom"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = MetricDomain::class,
            attributes = mapOf(
                "uri" to "MET",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "MET",
                "metrics" to dynamicAttribute { listOf(it.instantiate<Metric>()) },
                "hierarchies" to dynamicAttribute { listOf(it.instantiate<Hierarchy>()) }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Metric::class,
            attributes = mapOf(
                "uri" to "met_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "metricCode" to "sd1",
                "dataType" to "Enumeration/Code",
                "flowType" to "Stock",
                "balanceType" to "Credit",
                "referencedDomainCode" to "exp_dom",
                "referencedHierarchyCode" to "hier_code"
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = DpmDictionary::class,
            attributes = mapOf(
                "owner" to dynamicAttribute { it.instantiate<Owner>() },
                "metricDomains" to emptyList<ExplicitDomain>(),
                "explicitDomains" to emptyList<ExplicitDomain>(),
                "typedDomains" to emptyList<TypedDomain>(),
                "explicitDimensions" to emptyList<ExplicitDimension>(),
                "typedDimensions" to emptyList<TypedDimension>()
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = DpmModel::class,
            attributes = mapOf(
                "dictionaries" to listOf(dynamicAttribute { it.instantiate<DpmDictionary>() })
            )
        )
    )

    return definitions
}
