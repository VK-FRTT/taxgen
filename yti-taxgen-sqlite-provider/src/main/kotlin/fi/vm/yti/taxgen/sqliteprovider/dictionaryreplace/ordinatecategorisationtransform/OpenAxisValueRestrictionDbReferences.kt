package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import org.jetbrains.exposed.dao.EntityID

data class OpenAxisValueRestrictionDbReferences(
    val hierarchyId: EntityID<Int>?,
    val hierarchyStartingMemberId: EntityID<Int>?
) {
    fun validate(validationResults: ValidationResults) {

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionDbReferences::hierarchyId
        )

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = OpenAxisValueRestrictionDbReferences::hierarchyStartingMemberId
        )
    }
}
