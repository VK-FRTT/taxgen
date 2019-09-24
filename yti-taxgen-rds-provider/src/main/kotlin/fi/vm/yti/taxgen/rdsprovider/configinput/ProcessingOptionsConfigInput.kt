package fi.vm.yti.taxgen.rdsprovider.configinput

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.ProcessingOptions

data class ProcessingOptionsConfigInput(
    val sqliteDbDpmElementInherentTextLanguage: String?,
    val sqliteDbMandatoryLabelLanguage: String?,
    val sqliteDbMandatoryLabelSourceLanguages: List<String?>?,
    val sqliteDbDpmElementUriStorageLabelLanguage: String?
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
            sqliteDbDpmElementInherentTextLanguage = toOptionalLanguage(
                sqliteDbDpmElementInherentTextLanguage
            ),

            sqliteDbMandatoryLabelLanguage = toOptionalLanguage(
                sqliteDbMandatoryLabelLanguage
            ),
            sqliteDbMandatoryLabelSourceLanguages = toOptionalLanguages(
                sqliteDbMandatoryLabelSourceLanguages
            ),

            sqliteDbDpmElementUriStorageLabelLanguage = toOptionalLanguage(
                sqliteDbDpmElementUriStorageLabelLanguage
            )
        )
    }
}
