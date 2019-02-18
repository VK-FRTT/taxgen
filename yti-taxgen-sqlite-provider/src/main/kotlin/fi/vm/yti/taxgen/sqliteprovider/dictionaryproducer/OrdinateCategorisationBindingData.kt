package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonBlank
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import org.jetbrains.exposed.dao.EntityID

data class OrdinateCategorisationBindingData(
    val ordinateId: EntityID<Int>?,
    val dimensionMemberSignature: String?,
    val signatureDimensionPart: String,
    val signatureMemberPart: String,

    val source: String?,
    val dps: String?,

    val dimensionId: EntityID<Int>? = null,
    val memberId: EntityID<Int>? = null
) : Validatable {

    override fun validate(validationResults: ValidationResults) {

        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationBindingData::signatureDimensionPart
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationBindingData::signatureMemberPart
        )

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationBindingData::dimensionId
        )

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OrdinateCategorisationBindingData::memberId
        )
    }
}
