package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.datafactory.DataDefinition
import fi.vm.yti.taxgen.datapointmetamodel.datafactory.dynamicAttribute
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
                "languages" to dynamicAttribute {
                    setOf(
                        Language.findByIso6391Code("en"),
                        Language.findByIso6391Code("fi")
                    )
                },
                "defaultLanguage" to dynamicAttribute { Language.findByIso6391Code("fi") }
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
            kClass = Member::class,
            attributes = mapOf(
                "id" to "mem_1",
                "uri" to "mem_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "memberCode" to "exp_mc",
                "defaultMember" to true
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDomain::class,
            attributes = mapOf(
                "id" to "exp_dom_1",
                "uri" to "exp_dom_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "exp_dom",
                "members" to dynamicAttribute { listOf(it.instantiate<Member>()) },
                "hierarchies" to dynamicAttribute { listOf<Hierarchy>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = DpmDictionary::class,
            attributes = mapOf(
                "owner" to dynamicAttribute { it.instantiate<Owner>() },
                "explicitDomains" to dynamicAttribute { listOf(it.instantiate<ExplicitDomain>()) }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = Hierarchy::class,
            attributes = mapOf(
                "id" to "hie_1",
                "uri" to "hie_1_uri",
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
                "id" to "hie_nod_1",
                "uri" to "hie_nod_1_uri",
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "abstract" to false,
                "comparisonOperator" to "=",
                "unaryOperator" to "+",
                "memberRef" to dpmElementRef<Member>("mem_1", "uri_1", "diagnostic_label"),
                "childNodes" to listOf<HierarchyNode>()
            )
        )
    )

    return definitions
}
