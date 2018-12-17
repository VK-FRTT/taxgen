package fi.vm.yti.taxgen.rdsdpmmapper.extractor

import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsdpmmapper.DpmMappingContext

internal fun DpmSource.extractDpmDictionaries(
    ctx: DpmMappingContext
): List<DpmDictionary> {

    return ctx.extractList(this) {
        dpmDictionarySources().map { dictionarySource ->
            dictionarySource.extractDpmDictionary(ctx)
        }
    }
}
