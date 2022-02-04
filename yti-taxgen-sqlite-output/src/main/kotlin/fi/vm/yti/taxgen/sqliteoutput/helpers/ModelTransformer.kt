package fi.vm.yti.taxgen.sqliteoutput.helpers

import fi.vm.yti.taxgen.commons.diagnostic.DiagnosticContexts
import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.DpmModel
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Metric
import fi.vm.yti.taxgen.dpmmodel.MetricDomain
import fi.vm.yti.taxgen.dpmmodel.TranslatedText
import fi.vm.yti.taxgen.dpmmodel.TypedDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContext

internal object ModelTransformer {

    internal data class Context(
        val referencedElementConceptsByCode: Map<String, Concept>,
        val processingOptions: ProcessingOptions,
        val diagnostic: Diagnostic
    )

    fun transformDpmModelByProcessingOptions(
        originDpmModel: DpmModel,
        processingOptions: ProcessingOptions,
        diagnosticContext: DiagnosticContext
    ): DpmModel {
        return diagnosticContext.withContext(
            contextType = DiagnosticContexts.DpmModelProcessingOptionsTransform.toType(),
            contextDetails = null
        ) {
            doTransformDpmModelByProcessingOptions(
                originDpmModel = originDpmModel,
                processingOptions = processingOptions,
                diagnostic = diagnosticContext
            )
        }
    }

    private fun doTransformDpmModelByProcessingOptions(
        originDpmModel: DpmModel,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ): DpmModel {

        return DpmModel(
            dictionaries = originDpmModel.dictionaries.map { dpmDictionary ->

                val context = Context(
                    referencedElementConceptsByCode = emptyMap(),
                    processingOptions = processingOptions,
                    diagnostic = diagnostic
                )

                DpmDictionary(
                    owner = dpmDictionary.owner,

                    explicitDomains = transformDpmElements(
                        dpmDictionary.explicitDomains,
                        context
                    ),

                    typedDomains = transformDpmElements(
                        dpmDictionary.typedDomains,
                        context
                    ),

                    explicitDimensions = transformDpmElements(
                        dpmDictionary.explicitDimensions,
                        context
                    ),

                    typedDimensions = transformDpmElements(
                        dpmDictionary.typedDimensions,
                        context
                    ),

                    metricDomain = transformDpmElement(
                        dpmDictionary.metricDomain,
                        context
                    )
                )
            }
        )
    }

    private fun <T : DpmElement> transformDpmElement(
        originDpmElement: T?,
        context: Context
    ): T? {
        originDpmElement ?: return null

        return transformDpmElements(
            originDpmElements = listOf(originDpmElement),
            context = context
        ).first()
    }

    private fun <T : DpmElement> transformDpmElements(
        originDpmElements: List<T>,
        context: Context
    ): List<T> {

        return originDpmElements.map { originElement ->

            val transformedElement = when (originElement) {

                is ExplicitDimension -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        )
                    )
                }

                is ExplicitDomain -> {
                    val transformedMembers = transformDpmElements(
                        originElement.members,
                        context
                    )

                    val transformedHierarchies = transformDpmElements(
                        originElement.hierarchies,
                        context.copy(
                            referencedElementConceptsByCode = transformedMembers.map { it.memberCode to it.concept }
                                .toMap()
                        )
                    )

                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        ),
                        members = transformedMembers,
                        hierarchies = transformedHierarchies
                    )
                }

                is Hierarchy -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        ),
                        rootNodes = transformDpmElements(
                            originElement.rootNodes,
                            context
                        )
                    )
                }

                is HierarchyNode -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        ),
                        childNodes = transformDpmElements(
                            originElement.childNodes,
                            context
                        )
                    )
                }

                is Member -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        )
                    )
                }

                is Metric -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        )
                    )
                }

                is MetricDomain -> {
                    val transformedMetrics = transformDpmElements(
                        originElement.metrics,
                        context
                    )

                    val transformedHierarchies = transformDpmElements(
                        originElement.hierarchies,
                        context.copy(
                            referencedElementConceptsByCode = transformedMetrics.map { it.metricCode to it.concept }
                                .toMap()
                        )
                    )

                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        ),
                        metrics = transformedMetrics,
                        hierarchies = transformedHierarchies
                    )
                }

                is TypedDimension -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        )
                    )
                }

                is TypedDomain -> {
                    originElement.copy(
                        concept = transformConcept(
                            originElement,
                            context
                        )
                    )
                }

                else -> {
                    thisShouldNeverHappen("No transformation for class ${originElement::class}")
                }
            }

            @Suppress("UNCHECKED_CAST")
            transformedElement as T
        }
    }

    private fun transformConcept(
        dpmElement: DpmElement,
        context: Context
    ): Concept {

        val labelTranslations = dpmElement.concept.label.translations.toMutableMap()

        injectMandatoryLabelTranslationWhenRequested(
            labelTranslations,
            context.processingOptions
        )

        injectDpmElementUriAsTranslationWhenRequested(
            labelTranslations,
            dpmElement.uri,
            context.processingOptions,
            context.diagnostic
        )

        injectComposedHierarchyNodeTranslationsWhenRequested(
            labelTranslations,
            dpmElement,
            context.referencedElementConceptsByCode,
            context.processingOptions
        )

        return dpmElement.concept.copy(
            label = TranslatedText(labelTranslations)
        )
    }

    private fun injectMandatoryLabelTranslationWhenRequested(
        translations: MutableMap<Language, String>,
        processingOptions: ProcessingOptions
    ) {
        val targetLanguage = processingOptions.sqliteDbMandatoryLabelLanguage
        val sourceLanguages = processingOptions.sqliteDbMandatoryLabelSourceLanguages

        if (targetLanguage != null && sourceLanguages != null) {

            if (!translations.containsKey(targetLanguage)) {
                val sourceLanguage = sourceLanguages.find { translations.containsKey(it) }

                if (sourceLanguage != null) {
                    translations[targetLanguage] = translations.getValue(sourceLanguage)
                }
            }
        }
    }

    private fun injectDpmElementUriAsTranslationWhenRequested(
        translations: MutableMap<Language, String>,
        uri: String,
        processingOptions: ProcessingOptions,
        diagnostic: Diagnostic
    ) {
        val uriStorageLanguage = processingOptions.sqliteDbDpmElementUriStorageLabelLanguage

        if (uriStorageLanguage != null) {

            if (translations.containsKey(uriStorageLanguage)) {
                diagnostic.warning("DPM Element URI overwrites existing translation: ${translations[uriStorageLanguage]} (${uriStorageLanguage.iso6391Code})")
            }

            translations[uriStorageLanguage] = uri
        }
    }

    private fun injectComposedHierarchyNodeTranslationsWhenRequested(
        translations: MutableMap<Language, String>,
        dpmElement: DpmElement,
        referencedElementConceptsByCode: Map<String, Concept>,
        processingOptions: ProcessingOptions
    ) {
        val hierarchyNodeLabelCompositionLanguages = processingOptions.sqliteDbHierarchyNodeLabelCompositionLanguages

        if (dpmElement is HierarchyNode &&
            hierarchyNodeLabelCompositionLanguages != null
        ) {
            doInjectComposedTranslations(
                translations = translations,
                referencedElementLabelTranslations = selectReferencedElementLabelTranslations(
                    referencedElementConceptsByCode,
                    dpmElement.referencedElementCode
                ),
                compositionLanguages = hierarchyNodeLabelCompositionLanguages,
                fallbackTranslationLanguage = processingOptions.sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage
            )
        }
    }

    private fun selectReferencedElementLabelTranslations(
        referencedElementConceptsByCode: Map<String, Concept>,
        referencedElementCode: String
    ): Map<Language, String> {
        val referencedElementConcept = referencedElementConceptsByCode[referencedElementCode]
            ?: thisShouldNeverHappen("No Concept found for ReferencedElementCode: $referencedElementCode")

        return referencedElementConcept.label.translations
    }

    private fun doInjectComposedTranslations(
        translations: MutableMap<Language, String>,
        referencedElementLabelTranslations: Map<Language, String>,
        compositionLanguages: List<Language>,
        fallbackTranslationLanguage: Language?
    ) {
        val composedTranslations = compositionLanguages.map { language ->

            val composedTranslation = composeTranslation(
                translation = translations[language],
                referencedElementTranslation = referencedElementLabelTranslations[language],
                fallbackTranslation = translations[fallbackTranslationLanguage]
            )

            Pair(language, composedTranslation)
        }

        translations.putAll(composedTranslations)
    }

    private fun composeTranslation(
        translation: String?,
        referencedElementTranslation: String?,
        fallbackTranslation: String?
    ): String {

        val leadingPart = translation ?: fallbackTranslation

        return when {

            leadingPart != null && referencedElementTranslation != null -> {
                "$leadingPart $referencedElementTranslation"
            }

            leadingPart != null && referencedElementTranslation == null -> {
                leadingPart
            }

            leadingPart == null && referencedElementTranslation != null -> {
                referencedElementTranslation
            }

            leadingPart == null && referencedElementTranslation == null -> {
                ""
            }

            else -> {
                thisShouldNeverHappen("Composition logic mismatch")
            }
        }
    }
}
