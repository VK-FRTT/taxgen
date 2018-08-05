package fi.vm.yti.taxgen.ycltodpmmapper.extractor

import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.DpmMappingContext

internal fun YclSource.extractDpmDictionaries(
    ctx: DpmMappingContext
): List<DpmDictionary> {

    //return ctx.extract(this) { //TODO
    return dpmDictionarySources().map { dictionarySource ->
        dictionarySource.extractDpmDictionary(ctx)
    }
    //}
}
