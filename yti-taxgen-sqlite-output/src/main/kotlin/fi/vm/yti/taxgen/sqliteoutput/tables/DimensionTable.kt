package fi.vm.yti.taxgen.sqliteoutput.tables

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mDimension` (
 *   `DimensionID` INTEGER,
 *   `DimensionLabel` TEXT,
 *   `DimensionCode` TEXT,
 *   `DimensionDescription` TEXT,
 *   `DimensionXBRLCode` TEXT,
 *   `DomainID` INTEGER,
 *   `IsTypedDimension` BOOLEAN,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`DomainID`) REFERENCES `mDomain`(`DomainID`),
 *   PRIMARY KEY(`DimensionID`),
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`)
 * );
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - None
 */
object DimensionTable : IntIdTable(name = "mDimension", columnName = "DimensionID") {

    val dimensionLabelCol = text("DimensionLabel").nullable()

    val dimensionCodeCol = text("DimensionCode").nullable()

    val dimensionDescriptionCol = text("DimensionDescription").nullable()

    val dimensionXBRLCodeCol = text("DimensionXBRLCode").nullable()

    val domainIdCol = reference(
        name = "DomainID",
        foreign = DomainTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    val isTypedDimensionCol = bool("IsTypedDimension").nullable()

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun insertExplicitDimension(
        dimension: ExplicitDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        inherentTextLanguage: Language?
    ) {
        val domainRow = DomainTable.rowWhereDomainOwnerAndCode(owner, dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}, for Owner: ${owner.name}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[dimensionDescriptionCol] = dimension.concept.description.translationForLangOrNull(inherentTextLanguage)
            it[dimensionXBRLCodeCol] = dimensionXbrlCode(owner, dimension.dimensionCode)
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = dimensionConceptId
        }
    }

    fun insertTypedDimension(
        dimension: TypedDimension,
        owner: Owner,
        dimensionConceptId: EntityID<Int>,
        inherentTextLanguage: Language?
    ) {
        val domainRow = DomainTable.rowWhereDomainOwnerAndCode(owner, dimension.referencedDomainCode)
            ?: thisShouldNeverHappen("No Domain matching Dimension.ReferencedDomainCode: ${dimension.referencedDomainCode}")

        DimensionTable.insert {
            it[dimensionCodeCol] = dimension.dimensionCode
            it[dimensionLabelCol] = dimension.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[dimensionDescriptionCol] = dimension.concept.description.translationForLangOrNull(inherentTextLanguage)
            it[dimensionXBRLCodeCol] = dimensionXbrlCode(owner, dimension.dimensionCode)
            it[domainIdCol] = domainRow[DomainTable.id]
            it[isTypedDimensionCol] = true
            it[conceptIdCol] = dimensionConceptId
        }
    }

    fun rowWhereXbrlCode(xbrlCode: String): ResultRow? = select {
        DimensionTable.dimensionXBRLCodeCol.eq(xbrlCode)
    }.firstOrNull()

    fun dimensionXbrlCode(owner: Owner, dimensionCode: String) = "${owner.prefix}_dim:$dimensionCode"
}
