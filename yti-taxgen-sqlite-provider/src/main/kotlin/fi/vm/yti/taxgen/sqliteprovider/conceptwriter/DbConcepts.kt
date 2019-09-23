package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTime
import fi.vm.yti.taxgen.sqliteprovider.ext.java.toJodaDateTimeOrNull
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteprovider.tables.ConceptType
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

object DbConcepts {

    private val DPM_ELEMENT_TYPE_TO_CONCEPT_TYPE = mapOf(
        ExplicitDomain::class to ConceptType.DOMAIN,
        TypedDomain::class to ConceptType.DOMAIN,
        Member::class to ConceptType.MEMBER,
        Metric::class to ConceptType.MEMBER,
        Hierarchy::class to ConceptType.HIERARCHY,
        HierarchyNode::class to ConceptType.HIERARCHY_NODE,
        ExplicitDimension::class to ConceptType.DIMENSION,
        TypedDimension::class to ConceptType.DIMENSION
    )

    fun writeConceptAndTranslations(
        dpmElement: DpmElement,
        ownerId: EntityID<Int>,
        languageIds: Map<Language, EntityID<Int>>,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ): EntityID<Int> {

        val conceptType = DPM_ELEMENT_TYPE_TO_CONCEPT_TYPE[dpmElement::class]
            ?: thisShouldNeverHappen("No concept type mapping for class ${dpmElement::class}")

        val labelTranslations = dpmElement.concept.label.translations
            .let {
                injectMandatoryLabelTranslation(
                    it,
                    processingOptions
                )
            }.let {
                injectDpmElementUriToLabelTranslation(
                    it,
                    dpmElement.uri,
                    processingOptions,
                    diagnostic
                )
            }

        val conceptId = insertConcept(
            dpmElement.concept,
            conceptType,
            ownerId
        )

        labelTranslations.forEach { (language, text) ->
            insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.LABEL,
                language,
                text
            )
        }

        dpmElement.concept.description.translations.forEach { (language, text) ->
            insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.DESCRIPTION,
                language,
                text
            )
        }

        return conceptId
    }

    fun deleteAllConceptsAndTranslations(conceptType: ConceptType) {
        val conceptTypeString = conceptType.value

        val matchingConceptIds = ConceptTable
            .select { ConceptTable.conceptTypeCol eq conceptTypeString }
            .map { it[ConceptTable.id] }

        ConceptTranslationTable.deleteWhere { ConceptTranslationTable.conceptIdCol inList matchingConceptIds }
        ConceptTable.deleteWhere { ConceptTable.conceptTypeCol eq conceptTypeString }
    }

    private fun injectMandatoryLabelTranslation(
        translations: Map<Language, String>,
        processingOptions: ProcessingOptions
    ): Map<Language, String> {

        val targetLanguage = processingOptions.sqliteDbMandatoryLabelLanguage
        val sourceLanguages = processingOptions.sqliteDbMandatoryLabelSourceLanguages

        if (targetLanguage != null && sourceLanguages != null) {

            if (!translations.containsKey(targetLanguage)) {
                val sourceLanguage = sourceLanguages.find { translations.containsKey(it) }

                if (sourceLanguage != null) {
                    val mutableTranslations = translations.toMutableMap()
                    mutableTranslations[targetLanguage] = translations.getValue(sourceLanguage)

                    return mutableTranslations
                }
            }
        }

        return translations
    }

    private fun injectDpmElementUriToLabelTranslation(
        translations: Map<Language, String>,
        uri: String,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ): Map<Language, String> {

        val uriStorageLanguage = processingOptions.sqliteDbDpmElementUriStorageLabelLanguage
        if (uriStorageLanguage != null) {

            if (translations.containsKey(uriStorageLanguage)) {

                //TODO - make message as warning
                diagnostic.info("DPM Element URI overwrites existing translation: ${translations[uriStorageLanguage]} (${uriStorageLanguage.iso6391Code})")
            }

            val mutableTranslations = translations.toMutableMap()
            mutableTranslations[uriStorageLanguage] = uri

            return mutableTranslations
        }

        return translations
    }

    private fun insertConcept(
        concept: Concept,
        conceptType: ConceptType,
        ownerId: EntityID<Int>
    ): EntityID<Int> {

        return ConceptTable.insertAndGetId {
            it[conceptTypeCol] = conceptType.value
            it[ownerIdCol] = ownerId
            it[creationDateCol] = concept.createdAt.toJodaDateTime()
            it[modificationDateCol] = concept.modifiedAt.toJodaDateTime()
            it[fromDateCol] = concept.applicableFrom.toJodaDateTimeOrNull()
            it[toDateCol] = concept.applicableUntil.toJodaDateTimeOrNull()
        }
    }

    private fun insertConceptTranslation(
        languageIds: Map<Language, EntityID<Int>>,
        conceptId: EntityID<Int>,
        role: ConceptTranslationRole,
        language: Language,
        text: String
    ) {
        val languageId = languageIds[language] ?: thisShouldNeverHappen("Language without DB mapping: $language")

        ConceptTranslationTable.insert {
            it[conceptIdCol] = conceptId
            it[languageIdCol] = languageId
            it[textCol] = text
            it[roleCol] = role.value
        }
    }
}
