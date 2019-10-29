package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.datavalidation.Validatable
import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.datavalidation.validateElementValueUnique

data class DpmModel(
    val dictionaries: List<DpmDictionary>
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "dictionaries",
            iterable = dictionaries,
            valueSelector = { it: DpmDictionary -> it.owner.prefix },
            valueDescription = "owner.prefix"
        )

        //TODO - validate that Domain codes unique within all Dictionaries
    }
}
