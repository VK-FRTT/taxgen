package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.extractor.extractDpmDictionaries

class YclToDpmMapper(
    private val diagnostic: Diagnostic
) {

    fun getDpmDictionariesFromSource(
        yclSource: YclSource
    ): List<DpmDictionary> {

        return diagnostic.withContext(
            contextType = "Processing",
            contextName = "YCL Source data to DPM model"
        ) {

            val ctx = DpmMappingContext.createRootContext(diagnostic)

            yclSource.extractDpmDictionaries(ctx)
        }
    }
}
