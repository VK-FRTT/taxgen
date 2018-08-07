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
                    Language.findByIso6391Code("en") to "Something in english",
                    Language.findByIso6391Code("fi") to "Jotakin suomeksi"
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
                "defaultLanguage" to dynamicAttribute { Language.findByIso6391Code("en") }
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
                "concept" to dynamicAttribute { it.instantiate<Concept>() },
                "domainCode" to "exp_dc",
                "members" to dynamicAttribute { listOf(it.instantiate<Member>()) }
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

    return definitions
}
