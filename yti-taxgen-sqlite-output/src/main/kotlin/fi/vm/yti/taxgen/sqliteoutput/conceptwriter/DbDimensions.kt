package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.sqliteoutput.tables.DimensionTable
import org.jetbrains.exposed.dao.id.EntityID
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

            DimensionTable.insertExplicitDimension(
                dimension,
                owner,
                dimensionConceptId,
                processingOptions.sqliteDbDpmElementInherentTextLanguage
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

            DimensionTable.insertTypedDimension(
                dimension,
                owner,
                dimensionConceptId,
                processingOptions.sqliteDbDpmElementInherentTextLanguage
            )
        }
    }
}
