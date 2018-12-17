package fi.vm.yti.taxgen.rdsdpmmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextType
import fi.vm.yti.taxgen.dpmmodel.DpmDictionary
import fi.vm.yti.taxgen.rdsprovider.DpmSource
import fi.vm.yti.taxgen.rdsdpmmapper.extractor.extractDpmDictionaries

class RdsToDpmMapper(
    private val diagnostic: Diagnostic
) {

    fun getDpmDictionariesFromSource(
        dpmSource: DpmSource
    ): List<DpmDictionary> {

        return diagnostic.withContext(
            contextType = DiagnosticContextType.MappingRdsToDpm,
            contextLabel = "RDS source data to DPM model"
        ) {

            val ctx = DpmMappingContext.createRootContext(diagnostic)

            dpmSource.extractDpmDictionaries(ctx)
        }
    }
}
