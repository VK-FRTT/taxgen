package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.commons.throwFail
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import fi.vm.yti.taxgen.sqliteprovider.tables.DimensionTable
import fi.vm.yti.taxgen.sqliteprovider.tables.DomainTable
import fi.vm.yti.taxgen.sqliteprovider.tables.MemberTable
import fi.vm.yti.taxgen.sqliteprovider.tables.OwnerTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

object DbFixedEntities {

    fun writeFixedEntities(
        languageIds: Map<Language, EntityID<Int>>,
        diagnostic: Diagnostic
    ): FixedEntitiesLookupItem {
        val enLang = Language.byIso6391CodeOrFail("en")
        val enLanguageId = languageIds[enLang] ?: throwFail("Missing language id for language 'en'")

        return transaction {
            val metricDomainOwnerId = lookupEurofilingOwnerId(diagnostic)

            val (metricDomainId, metricDomainCode) = writeMetricDomain(metricDomainOwnerId, enLanguageId)
            val (metricDimensionId, metricDimensionCode) = writeMetricDimension(
                metricDomainOwnerId,
                metricDomainId,
                enLanguageId
            )

            val openMemberId = writeOpenDomainAndMember()

            FixedEntitiesLookupItem(
                metricDomainOwnerId = metricDomainOwnerId,
                metricDomainCode = metricDomainCode,
                metricDomainId = metricDomainId,

                metricDimensionXbrlCode = metricDimensionCode,
                metricDimensionId = metricDimensionId,

                openMemberId = openMemberId
            )
        }
    }

    private fun writeOpenDomainAndMember(): EntityID<Int> {
        TransactionManager
            .current()
            .exec(
                """
                INSERT INTO mDomain(DomainID, DomainCode, DomainLabel, IsTypedDomain)
                VALUES (9999, "9999", "Open", 0)
                """.trimIndent()
            )

        TransactionManager
            .current()
            .exec(
                """
                INSERT INTO mMember(MemberID, DomainID, MemberLabel)
                VALUES (9999, 9999, "Open")
                """.trimIndent()
            )

        return EntityID(9999, MemberTable)
    }

    private fun writeMetricDomain(
        ownerId: EntityID<Int>,
        enLanguageId: EntityID<Int>
    ): Pair<EntityID<Int>, String> {

        val label = "Metrics"
        val code = "MET"

        val domainConceptId = insertConceptAndTranslation(
            ConceptType.DOMAIN,
            ownerId,
            DateTime(0),
            enLanguageId,
            label
        )

        val domainId = insertDomain(
            code,
            code,
            label,
            domainConceptId
        )

        return Pair(domainId, code)
    }

    private fun writeMetricDimension(
        ownerId: EntityID<Int>,
        domainId: EntityID<Int>,
        enLanguageId: EntityID<Int>
    ): Pair<EntityID<Int>, String> {
        val label = "Metric dimension"
        val code = "MET"

        val dimensionConceptId = insertConceptAndTranslation(
            ConceptType.DIMENSION,
            ownerId,
            null,
            enLanguageId,
            label
        )

        val dimensionId = insertDimension(
            domainId,
            code,
            code,
            label,
            dimensionConceptId
        )

        return Pair(dimensionId, code)
    }

    private fun lookupEurofilingOwnerId(
        diagnostic: Diagnostic
    ): EntityID<Int> {
        return transaction {
            val matchingRows = OwnerTable.select { OwnerTable.ownerPrefixCol eq "eu" }

            if (matchingRows.count() != 1) {
                diagnostic.fatal("Selecting 'Eurofiling' Owner from database failed. Found ${matchingRows.count()} Owners with prefix 'eu'.")
            }

            matchingRows.first()[OwnerTable.id]
        }
    }

    private fun insertConceptAndTranslation(
        conceptType: ConceptType,
        ownerId: EntityID<Int>,
        fromDate: DateTime?,
        enLanguageId: EntityID<Int>,
        enLabel: String
    ): EntityID<Int> {

        val conceptId = ConceptTable.insertAndGetId {
            it[conceptTypeCol] = conceptType.value
            it[ownerIdCol] = ownerId
            it[creationDateCol] = null
            it[modificationDateCol] = null
            it[fromDateCol] = fromDate
            it[toDateCol] = null
        }

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = enLanguageId
            it[textCol] = enLabel
            it[roleCol] = ConceptTranslationRole.LABEL.value
        }

        return conceptId
    }

    private fun insertDomain(
        domainCode: String,
        domainXbrlCode: String?,
        domainDefaultLabel: String,
        conceptId: EntityID<Int>?
    ): EntityID<Int> {
        return DomainTable.insertAndGetId {
            it[domainCodeCol] = domainCode
            it[domainLabelCol] = domainDefaultLabel
            it[domainDescriptionCol] = null
            it[domainXBRLCodeCol] = domainXbrlCode
            it[dataTypeCol] = null
            it[isTypedDomainCol] = false
            it[conceptIdCol] = conceptId
        }
    }

    private fun insertDimension(
        domainId: EntityID<Int>,
        dimensionCode: String,
        dimensionXbrlCode: String,
        dimensionDefaultLabel: String,
        conceptId: EntityID<Int>
    ): EntityID<Int> {
        return DimensionTable.insertAndGetId {
            it[dimensionCodeCol] = dimensionCode
            it[dimensionLabelCol] = dimensionDefaultLabel
            it[dimensionDescriptionCol] = null
            it[dimensionXBRLCodeCol] = dimensionXbrlCode
            it[domainIdCol] = domainId
            it[isTypedDimensionCol] = false
            it[conceptIdCol] = conceptId
        }
    }
}
