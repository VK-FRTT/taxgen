package fi.vm.yti.taxgen.sqliteprovider.writers

import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.sqliteprovider.conceptitems.DpmDictionaryItem
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object DbDimensions {
    fun writeExplicitDimension(
        dictionaryItem: DpmDictionaryItem,
        dimension: ExplicitDimension
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                dimension.concept,
                ConceptType.DIMENSION
            )

            insertExplicitDimension(
                dictionaryItem,
                dimension,
                dimensionConceptId
            )
        }
    }

    fun writeTypedDimension(
        dictionaryItem: DpmDictionaryItem,
        dimension: TypedDimension
    ) {
        transaction {
            val dimensionConceptId = DbConcepts.writeConceptAndTranslations(
                dictionaryItem,
                dimension.concept,
                ConceptType.DIMENSION
            )

            insertTypedDimension(
                dictionaryItem,
                dimension,
                dimensionConceptId
            )
        }
    }

    private fun insertExplicitDimension(
        dictionaryItem: DpmDictionaryItem,
        dimension: ExplicitDimension,
        dimensionConceptId: EntityID<Int>
    ) {
        val dimensionXbrlCode = "${dictionaryItem.owner.prefix}_dim:${dimension.dimensionCode}"

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslation()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslation()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = dimensionConceptId
        }
    }

    private fun insertTypedDimension(
        dictionaryItem: DpmDictionaryItem,
        dimension: TypedDimension,
        dimensionConceptId: EntityID<Int>
    ) {
        val dimensionXbrlCode = "${dictionaryItem.owner.prefix}_dim:${dimension.dimensionCode}"

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.defaultTranslation()
            it[dimensionDescriptionCol] = dimension.concept.description.defaultTranslation()
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[isTypedDimensionCol] = true
            it[conceptIdCol] = dimensionConceptId
        }
    }
}
