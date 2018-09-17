package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class Hierarchy(
    val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : Validatable {

    override fun validate(validationErrors: ValidationErrors) {

        concept.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Hierarchy::hierarchyCode,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Hierarchy::rootNodes,
            minLength = 1,
            maxLength = 10000
        )
    }
}
