package fi.vm.yti.taxgen.sqliteoutput.conceptwriter

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTable
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTranslationRole
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.sqliteoutput.tables.ConceptType
import org.jetbrains.exposed.dao.id.EntityID

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
        languageIds: Map<Language, EntityID<Int>>
    ): EntityID<Int> {

        val conceptType = DPM_ELEMENT_TYPE_TO_CONCEPT_TYPE[dpmElement::class]
            ?: thisShouldNeverHappen("No concept type mapping for class ${dpmElement::class}")

        val conceptId = ConceptTable.insertConcept(
            dpmElement.concept,
            conceptType,
            ownerId
        )

        dpmElement.concept.label.translations.forEach { (language, text) ->
            ConceptTranslationTable.insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.LABEL,
                language,
                text
            )
        }

        dpmElement.concept.description.translations.forEach { (language, text) ->
            ConceptTranslationTable.insertConceptTranslation(
                languageIds,
                conceptId,
                ConceptTranslationRole.DESCRIPTION,
                language,
                text
            )
        }

        return conceptId
    }
}
