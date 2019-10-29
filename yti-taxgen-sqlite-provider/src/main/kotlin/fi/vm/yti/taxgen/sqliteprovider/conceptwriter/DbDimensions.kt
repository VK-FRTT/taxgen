package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
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
        processingOptions: ProcessingOptions
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds
            )

            insertExplicitDimension(
                dimension,
                owner,
                dimensionConceptId,
                processingOptions
            )
        }
    }

    fun writeTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dimension,
                ownerId,
                languageIds
            )

            insertTypedDimension(
                dimension,
                owner,
                dimensionConceptId,
                processingOptions
            )
        }
    }

    private fun insertExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        processingOptions: ProcessingOptions
    ) {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"

        val domainRow = DomainTable.rowWhereDomainCode(dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] =
                dimension.concept.label.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[dimensionDescriptionCol] =
                dimension.concept.description.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = dimensionConceptId
        }
    }

    private fun insertTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        processingOptions: ProcessingOptions
    ) {
        val dimensionXbrlCode = "${owner.prefix}_dim:${dimension.dimensionCode}"

        val domainRow = DomainTable.rowWhereDomainCode(dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] =
                dimension.concept.label.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[dimensionDescriptionCol] =
                dimension.concept.description.translationForLangOrNull(processingOptions.sqliteDbDpmElementInherentTextLanguage)
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = true
            it[conceptIdCol] = dimensionConceptId
        }
    }
}
