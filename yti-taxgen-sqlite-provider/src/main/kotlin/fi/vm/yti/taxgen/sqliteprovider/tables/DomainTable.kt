package fi.vm.yti.taxgen.sqliteprovider.tables

import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

/**
 * Reference DDL (from BR-AG Data Modeler):
 * CREATE TABLE `mDomain` (
 *   `DomainID` INTEGER NOT NULL,
 *   `DomainCode` TEXT,
 *   `DomainLabel` TEXT,
 *   `DomainDescription` TEXT,
 *   `DomainXBRLCode` TEXT,
 *   `DataType` TEXT,
 *   `IsTypedDomain` BOOLEAN,
 *   `ConceptID` INTEGER,
 *   FOREIGN KEY(`ConceptID`) REFERENCES `mConcept`(`ConceptID`),
 *   PRIMARY KEY(`DomainID`)
 * );
 *
 * DataType reference values (from BR-AG Data Modeler):
 * - `String`
 * - `Percent`
 * - `Monetary`
 * - `Integer`
 * - `Decimal`
 * - `Date`
 * - `Boolean`
 *
 * Entity differences between the reference (BR-AG DM) and Tool for Undertakings (T4U) specification:
 * - T4U defines DataType as INTEGER (most likely error in spec)
 */
object DomainTable : IntIdTable(name = "mDomain", columnName = "DomainID") {

    val domainCodeCol = text("DomainCode").nullable()

    val domainLabelCol = text("DomainLabel").nullable()

    val domainDescriptionCol = text("DomainDescription").nullable()

    val domainXBRLCodeCol = text("DomainXBRLCode").nullable()

    val dataTypeCol = text("DataType").nullable()

    val isTypedDomainCol = bool("IsTypedDomain").nullable()

    val conceptIdCol = reference(
        name = "ConceptID",
        foreign = ConceptTable,
        onDelete = ReferenceOption.NO_ACTION,
        onUpdate = ReferenceOption.NO_ACTION
    ).nullable()

    fun insertExplicitDomain(
        domain: ExplicitDomain,
        domainConceptId: EntityID<Int>,
        owner: Owner,
        inherentTextLanguage: Language?
    ): EntityID<Int> {
        val domainId = DomainTable.insertAndGetId {
            it[domainCodeCol] = domain.domainCode
            it[domainLabelCol] = domain.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[domainDescriptionCol] = domain.concept.description.translationForLangOrNull(inherentTextLanguage)
            it[domainXBRLCodeCol] = explicitDomainXbrlCode(owner, domain.domainCode)
            it[dataTypeCol] = null
            it[isTypedDomainCol] = false
            it[conceptIdCol] = domainConceptId
        }

        return domainId
    }

    fun insertTypedDomain(
        domain: TypedDomain,
        domainConceptId: EntityID<Int>,
        owner: Owner,
        inherentTextLanguage: Language?
    ): EntityID<Int> {

        return DomainTable.insertAndGetId {
            it[domainCodeCol] = domain.domainCode
            it[domainLabelCol] = domain.concept.label.translationForLangOrNull(inherentTextLanguage)
            it[domainDescriptionCol] = domain.concept.description.translationForLangOrNull(inherentTextLanguage)
            it[domainXBRLCodeCol] = typedDomainXbrlCode(owner, domain.domainCode)
            it[dataTypeCol] = domain.dataType
            it[isTypedDomainCol] = true
            it[conceptIdCol] = domainConceptId
        }
    }

    fun rowWhereDomainId(domainId: EntityID<Int>) = select {
        DomainTable.id.eq(domainId)
    }.firstOrNull()

    fun rowWhereDomainCode(domainCode: String) = select {
        DomainTable.domainCodeCol.eq(domainCode)
    }.firstOrNull()

    fun rowWhereDomainXbrlCode(domainXbrlCode: String) = select {
        DomainTable.domainXBRLCodeCol.eq(domainXbrlCode)
    }.firstOrNull()

    fun explicitDomainXbrlCode(owner: Owner, domainCode: String) = "${owner.prefix}_exp:$domainCode"

    fun typedDomainXbrlCode(owner: Owner, domainCode: String) = "${owner.prefix}_typ:$domainCode"
}
