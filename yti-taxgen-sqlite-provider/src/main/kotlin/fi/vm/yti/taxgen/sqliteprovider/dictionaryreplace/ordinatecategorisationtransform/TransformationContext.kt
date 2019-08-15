package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.FixedEntitiesLookupItem
import org.jetbrains.exposed.dao.EntityID

data class TransformationContext(
    val dimensionIdsByXbrlCodes: Map<String, EntityID<Int>>,
    val memberIdsByXbrlCodes: Map<String, EntityID<Int>>,
    val fixedEntitiesLookupItem: FixedEntitiesLookupItem
) {

    companion object {

        fun fromLookupItems(
            dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>,
            fixedEntitiesLookupItem: FixedEntitiesLookupItem
        ): TransformationContext {
            return TransformationContext(
                dimensionIdsByXbrlCodes = collectDimensionIdsByXbrlCode(dpmDictionaryLookupItems),
                memberIdsByXbrlCodes = collectMemberIdsByXbrlCode(dpmDictionaryLookupItems),
                fixedEntitiesLookupItem = fixedEntitiesLookupItem
            )
        }

        private fun collectDimensionIdsByXbrlCode(
            dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>
        ): Map<String, EntityID<Int>> {

            return dpmDictionaryLookupItems.flatMap { dictionaryItem ->
                dictionaryItem.dimensionLookupItems.map { dimensionItem ->
                    Pair(dimensionItem.dimensionXbrlCode, dimensionItem.dimensionId)
                }
            }.toMap()
        }

        private fun collectMemberIdsByXbrlCode(
            dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>
        ): Map<String, EntityID<Int>> {

            return dpmDictionaryLookupItems.flatMap { dictionaryItem ->
                dictionaryItem.domainLookupItems.flatMap { domainItem ->
                    domainItem.memberLookupItems.map { memberItem ->
                        Pair(memberItem.memberXbrlCode, memberItem.memberId)
                    }
                }
            }.toMap()
        }
    }
}
