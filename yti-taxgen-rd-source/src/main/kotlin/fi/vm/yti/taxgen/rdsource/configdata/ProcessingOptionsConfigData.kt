package fi.vm.yti.taxgen.rdsource.configdata

import fi.vm.yti.taxgen.commons.processingoptions.ProcessingOptions
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import kotlin.reflect.KProperty0

data class ProcessingOptionsConfigData(
    val diagnosticSourceLanguages: List<String?>?,
    val sqliteDbDpmElementInherentTextLanguage: String?,
    val sqliteDbMandatoryLabelLanguage: String?,
    val sqliteDbMandatoryLabelSourceLanguages: List<String?>?,
    val sqliteDbDpmElementUriStorageLabelLanguage: String?,
    val sqliteDbHierarchyNodeLabelCompositionLanguages: List<String?>?,
    val sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage: String?
) {
    fun toProcessingOptions(diagnostic: Diagnostic): ProcessingOptions {

        fun toLanguage(iso6391Code: String?, propertyName: String): Language {
            return iso6391Code?.run { Language.findByIso6391Code(iso6391Code) }
                ?: diagnostic.fatal("$propertyName: Unsupported Language code '$iso6391Code'")
        }

        fun toLanguages(iso6391CodesProperty: KProperty0<List<String?>?>): List<Language> {
            val propertyName = iso6391CodesProperty.name
            val iso6391Codes = iso6391CodesProperty.get() ?: diagnostic.fatal("$propertyName: Language codes list is NULL")

            return iso6391Codes.map { iso6391Code ->
                toLanguage(iso6391Code, propertyName)
            }
        }

        fun toOptionalLanguage(iso6391CodeProperty: KProperty0<String?>): Language? {
            val iso6391Code = iso6391CodeProperty.get() ?: return null

            return toLanguage(iso6391Code, iso6391CodeProperty.name)
        }

        fun toOptionalLanguages(iso6391CodesProperty: KProperty0<List<String?>?>): List<Language>? {
            val iso6391Codes = iso6391CodesProperty.get() ?: return null
            val propertyName = iso6391CodesProperty.name

            return iso6391Codes.map { iso6391Code ->
                toLanguage(iso6391Code, propertyName)
            }
        }

        return ProcessingOptions(
            diagnosticSourceLanguages = toLanguages(
                this::diagnosticSourceLanguages
            ),

            sqliteDbDpmElementInherentTextLanguage = toOptionalLanguage(
                this::sqliteDbDpmElementInherentTextLanguage
            ),

            sqliteDbMandatoryLabelLanguage = toOptionalLanguage(
                this::sqliteDbMandatoryLabelLanguage
            ),
            sqliteDbMandatoryLabelSourceLanguages = toOptionalLanguages(
                this::sqliteDbMandatoryLabelSourceLanguages
            ),

            sqliteDbDpmElementUriStorageLabelLanguage = toOptionalLanguage(
                this::sqliteDbDpmElementUriStorageLabelLanguage
            ),

            sqliteDbHierarchyNodeLabelCompositionLanguages = toOptionalLanguages(
                this::sqliteDbHierarchyNodeLabelCompositionLanguages
            ),

            sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage = toOptionalLanguage(
                this::sqliteDbHierarchyNodeLabelCompositionNodeFallbackLanguage
            )
        )
    }
}
