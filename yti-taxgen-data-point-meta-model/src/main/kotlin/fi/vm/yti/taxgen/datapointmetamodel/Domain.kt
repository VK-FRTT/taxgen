package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.datapointmetamodel.validationfw.validateProperty

interface Domain {
    val concept: Concept
    val domainCode: String

    fun domainValidation() {
        validateProperty(
            instance = this,
            property = "domainCode",
            minLength = 2,
            maxLength = 50
        )
    }
}
