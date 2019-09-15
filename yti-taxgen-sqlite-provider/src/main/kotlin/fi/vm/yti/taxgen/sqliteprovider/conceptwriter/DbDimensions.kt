package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DbDimensions {
    fun writeExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds,
                processingOptions,
                diagnostic
            )

            insertExplicitDimension(
                dimension,
                owner,
                dimensionConceptId
            )
        }
    }

    fun writeTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds,
                processingOptions,
                diagnostic
            )

            insertTypedDimension(
                dimension,
                owner,
                dimensionConceptId
            )
        }
    }

    private fun insertExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>
    ) {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"

        val domainRow = DomainTable.rowWhereDomainCode(dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslationOrNull()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslationOrNull()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = dimensionConceptId
        }
    }

    private fun insertTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>
    ) {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"

        val domainRow = DomainTable.rowWhereDomainCode(dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslationOrNull()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslationOrNull()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = true
            it[conceptIdCol] = dimensionConceptId
        }
    }
}
