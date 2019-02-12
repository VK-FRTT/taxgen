package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import org.jetbrains.exposed.dao.EntityID

data class OrdinateCategorisationData(
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
        //TODO - validation
        //signatureDimensionPart NOT BLANK
        //signatureMemberPart NOT BLANK

        //dimensionId NOT NULL
        //memberId NOT NULL
    }
}
