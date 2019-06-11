package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.DpmModelOptions
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DimensionLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DomainLookupItem
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbDimensions {
    fun writeExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        domainLookupItems: List<DomainLookupItem>,
        modelOptions: Map<DpmModelOptions, Any>,
        diagnostic: Diagnostic
    ): DimensionLookupItem {
        return transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds,
                modelOptions,
                diagnostic
            )

            val (dimensionId, dimensionXbrlCode) = insertExplicitDimension(
                dimension,
                owner,
                dimensionConceptId,
                domainLookupItems
            )

            DimensionLookupItem(
                dimensionXbrlCode = dimensionXbrlCode,
                dimensionId = dimensionId
            )
        }
    }

    fun writeTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        domainLookupItems: List<DomainLookupItem>,
        modelOptions: Map<DpmModelOptions, Any>,
        diagnostic: Diagnostic
    ): DimensionLookupItem {
        return transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds,
                modelOptions,
                diagnostic
            )

            val (dimensionId, dimensionXbrlCode) = insertTypedDimension(
                dimension,
                owner,
                dimensionConceptId,
                domainLookupItems
            )

            DimensionLookupItem(
                dimensionXbrlCode = dimensionXbrlCode,
                dimensionId = dimensionId
            )
        }
    }

    private fun insertExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        domainLookupItems: List<DomainLookupItem>
    ): Pair<EntityID<Int>, String> {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"
        val referencedDomainItem = domainLookupItems.find { it.domainCode == dimension.referencedDomainCode }
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        val dimensionId = DimensionTable.insertAndGetId {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslationOrNull()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslationOrNull()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = referencedDomainItem.domainId
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = dimensionConceptId
        }

        return Pair(dimensionId, dimensionXbrlCode)
    }

    private fun insertTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        domainLookupItems: List<DomainLookupItem>
    ): Pair<EntityID<Int>, String> {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"
        val referencedDomainItem = domainLookupItems.find { it.domainCode == dimension.referencedDomainCode }
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        val dimensionId = DimensionTable.insertAndGetId {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslationOrNull()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslationOrNull()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = referencedDomainItem.domainId
            it[isTypedDimensionCol] = true
            it[conceptIdCol] = dimensionConceptId
        }

        return Pair(dimensionId, dimensionXbrlCode)
    }
}
