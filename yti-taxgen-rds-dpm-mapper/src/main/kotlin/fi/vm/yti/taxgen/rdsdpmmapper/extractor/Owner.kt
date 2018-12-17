package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig

internal fun Owner.Companion.fromConfig(
    ownerConfig: OwnerConfig,
    diagnostic: Diagnostic
): Owner {

    fun findLanguage(code: String?): Language {
        if (code == null || code.isBlank()) {
            diagnostic.fatal("Empty or null language code")
        }

        return Language.findByIso6391Code(code) ?: diagnostic.fatal("Unsupported language: '$code'")
    }

    return Owner(
        name = ownerConfig.name,
        namespace = ownerConfig.namespace,
        prefix = ownerConfig.prefix,
        location = ownerConfig.location,
        copyright = ownerConfig.copyright,
        languages = ownerConfig.languages.map { findLanguage(it) }.toSet(),
        defaultLanguage = findLanguage(ownerConfig.defaultLanguage)
    )
}
