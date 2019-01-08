package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsprovider.config.OwnerConfig

internal fun mapAndValidateOwner(
    ownerConfig: OwnerConfig,
    diagnostic: Diagnostic
): Owner {

    val owner = Owner(
        name = ownerConfig.name ?: "",
        namespace = ownerConfig.namespace ?: "",
        prefix = ownerConfig.prefix ?: "",
        location = ownerConfig.location ?: "",
        copyright = ownerConfig.copyright ?: "",
        languageCodes = ownerConfig.languages?.map { it ?: "" } ?: emptyList(),
        defaultLanguageCode = ownerConfig.defaultLanguage ?: ""
    )

    diagnostic.validate(owner)

    return owner
}
