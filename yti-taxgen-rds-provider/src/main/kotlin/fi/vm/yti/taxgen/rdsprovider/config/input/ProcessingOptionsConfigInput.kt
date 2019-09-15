package fi.vm.yti.taxgen.rdsprovider.config.input

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions

data class ProcessingOptionsConfigInput(
    val sqliteDbMandatoryLabelTranslationLanguage: String?,
    val sqliteDbMandatoryLabelTranslationSourceCandidateLanguages: List<String?>?,
    val sqliteDbDpmElementUriStorageLabelTranslationLanguage: String?
) {
    fun toProcessingOptions(diagnostic: Diagnostic): ProcessingOptions {

        fun toLanguage(iso6391Code: String): Language {
            return Language.findByIso6391Code(iso6391Code)
                ?: diagnostic.fatal("No Language for iso6391Code: $iso6391Code")
        }

        fun toOptionalLanguage(iso6391Code: String?): Language? {
            return iso6391Code?.let {
                toLanguage(it)
            }
        }

        fun toOptionalLanguages(iso6391Codes: List<String?>?): List<Language>? {
            iso6391Codes ?: return null

            return iso6391Codes.map { iso6391Code ->
                iso6391Code ?: diagnostic.fatal("NULL Language code")
                toLanguage(iso6391Code)
            }
        }

        return ProcessingOptions(
            sqliteDbMandatoryLabelTranslationLanguage = toOptionalLanguage(
                sqliteDbMandatoryLabelTranslationLanguage
            ),
            sqliteDbMandatoryLabelTranslationSourceCandidateLanguages = toOptionalLanguages(
                sqliteDbMandatoryLabelTranslationSourceCandidateLanguages
            ),

            sqliteDbDpmElementUriStorageLabelTranslationLanguage = toOptionalLanguage(
                sqliteDbDpmElementUriStorageLabelTranslationLanguage
            )
        )
    }
}
