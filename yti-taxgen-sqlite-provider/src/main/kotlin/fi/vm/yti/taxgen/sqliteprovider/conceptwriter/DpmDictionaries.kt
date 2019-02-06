package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.conceptitem.DomainItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitem.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.conceptitem.HierarchyItem
import org.jetbrains.exposed.dao.EntityID

object DpmDictionaries {

    fun writeDpmDictionaries(
        dpmDictionaries: List<DpmDictionary>,
        languageIds: Map<Language, EntityID<Int>>
    ) {
        dpmDictionaries.forEach { writeDictionary(it, languageIds) }
    }

    private fun writeDictionary(
        dpmDictionary: DpmDictionary,
        languageIds: Map<Language, EntityID<Int>>
    ) {
        val ownerId = DbOwners.writeOwner(dpmDictionary.owner)

        val dpmDictionaryItem = DpmDictionaryItem(
            dpmDictionary.owner,
            ownerId,
            languageIds
        )

        val explicitDomainItems = dpmDictionary.explicitDomains.map { explicitDomain ->

            val (explicitDomainId, memberItems) = DbDomains.writeExplicitDomainAndMembers(
                dpmDictionaryItem,
                explicitDomain
            )

            val hierarchyItems = explicitDomain.hierarchies.map { hierarchy ->
                val hierarchyId = DbHierarchies.writeHierarchyAndAndNodes(
                    dpmDictionaryItem,
                    hierarchy,
                    explicitDomainId,
                    memberItems
                )

                HierarchyItem(
                    hierarchyCode = hierarchy.hierarchyCode,
                    hierarchyId = hierarchyId
                )
            }

            DomainItem(
                domainCode = explicitDomain.domainCode,
                domainId = explicitDomainId,
                hierarchyItems = hierarchyItems
            )
        }

        dpmDictionaryItem.addDomainItems(explicitDomainItems)

        val typedDomainItems = dpmDictionary.typedDomains.map { typedDomain ->
            val typedDomainId = DbDomains.writeTypedDomain(
                dpmDictionaryItem,
                typedDomain
            )

            DomainItem(
                domainCode = typedDomain.domainCode,
                domainId = typedDomainId,
                hierarchyItems = emptyList()
            )
        }
        dpmDictionaryItem.addDomainItems(typedDomainItems)

        dpmDictionary.explicitDimensions.forEach { explicitDimension ->
            DbDimensions.writeExplicitDimension(
                dpmDictionaryItem,
                explicitDimension
            )
        }

        dpmDictionary.typedDimensions.forEach { typedDimension ->
            DbDimensions.writeTypedDimension(
                dpmDictionaryItem,
                typedDimension
            )
        }

        //TODO - insert "Open" member  with ID 9999

        dpmDictionary.metricDomains.map { metricDomain ->

            //TODO - Metric members IDs should start from 10000
            val (domainId, memberIds) = DbMetric.writeMetricDomainMembers(
                dpmDictionaryItem,
                metricDomain
            )

            metricDomain.hierarchies.map { hierarchy ->
                DbHierarchies.writeHierarchyAndAndNodes(
                    dpmDictionaryItem,
                    hierarchy,
                    domainId,
                    memberIds
                )
            }
        }
    }
}
