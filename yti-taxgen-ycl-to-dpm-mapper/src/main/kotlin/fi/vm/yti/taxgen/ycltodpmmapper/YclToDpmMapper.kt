package fi.vm.yti.taxgen.ycltodpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.datapointmetamodel.DpmDictionary
import fi.vm.yti.taxgen.yclsourceprovider.YclSource
import fi.vm.yti.taxgen.ycltodpmmapper.extractor.extractDpmDictionaries

class YclToDpmMapper(
    val diagnostic: Diagnostic
) {

    fun getDpmDictionariesFromSource(
        yclSource: YclSource
    ): List<DpmDictionary> {

        return diagnostic.withinTopic(
            topicType = "Processing",
            topicName = "YCL Source data to DPM model"
        ) {

            val ctx = DpmMappingContext.createRootContext(diagnostic)

            yclSource.extractDpmDictionaries(ctx)
        }
    }
}
