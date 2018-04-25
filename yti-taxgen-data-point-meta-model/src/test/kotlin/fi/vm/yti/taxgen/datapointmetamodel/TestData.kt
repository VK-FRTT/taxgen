package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.DataDefinition
import fi.vm.yti.taxgen.datapointmetamodel.testdataframework.dynamicAttribute
import java.time.LocalDate


fun dataPointMetaModelTestData(): Set<DataDefinition> {
    var definitions = HashSet<DataDefinition>()


    definitions.add(
        DataDefinition(
            kClass = TranslatedText::class,
            attributes = mapOf(
                "translations" to mapOf(
                    "fi" to "foo",
                    "en" to "bar"
                )
            )
        )
    )


    definitions.add(
        DataDefinition(
            kClass = Owner::class,
            attributes = mapOf(
                "namespace" to "namespace_value",
                "namespacePrefix" to "ns_prefix",
                "officialLocation" to "official_location",
                "copyrightText" to "Lorem ipsum",
                "supportedLanguages" to listOf("fi", "en")
            )
        )
    )


    definitions.add(
        DataDefinition(
            kClass = Concept::class,
            attributes = mapOf(
                "owner" to dynamicAttribute { it.instantiate<Owner>() },
                "createdAt" to LocalDate.of(2018, 1, 20),
                "modifiedAt" to LocalDate.of(2018, 2, 20),
                "applicableFrom" to LocalDate.of(2018, 3, 20),
                "applicableUntil" to LocalDate.of(2018, 4, 20),
                "label" to dynamicAttribute { it.instantiate<TranslatedText>() },
                "description" to dynamicAttribute { it.instantiate<TranslatedText>() }
            )
        )
    )

    definitions.add(
        DataDefinition(
            kClass = ExplicitDomainMember::class,
            attributes = mapOf(
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "memberCode" to "exp_mc"
            )
        )
    )


    definitions.add(
        DataDefinition(
            kClass = ExplicitDomain::class,
            attributes = mapOf(
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "exp_dc",
                "members" to dynamicAttribute { listOf(it.instantiate<ExplicitDomainMember>()) },
                "defaultMemberIndex" to 0
            )
        )
    )


    definitions.add(
        DataDefinition(
            kClass = TypedDomain::class,
            attributes = mapOf(
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "typ_dc"
            )
        )
    )


    return definitions
}


