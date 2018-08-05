package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.extractor.extractDpmDictionaries

class YclToDpmMapper {

    fun getDpmDictionariesFromSource(
        diagnostic: Diagnostic,
        yclSource: YclSource
    ): List<DpmDictionary> {
        val ctx = DpmMappingContext.createRootContext(diagnostic)
        return yclSource.extractDpmDictionaries(ctx)
    }
}
