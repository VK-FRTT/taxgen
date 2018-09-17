package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.customValidate
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

        customValidate(
            validationErrors = validationErrors,
            instance = this,
            property = Hierarchy::rootNodes,
            failIf = {
                val duplicates =
                    allNodes().map { it.member.memberCode }.groupingBy { it }.eachCount().filter { it.value > 1 }.keys
                it["duplicates"] = duplicates
                duplicates.any()
            },
            failMsg = { "contains duplicate members ${it["duplicates"]}" }
        )
    }

    private fun allNodes(): List<HierarchyNode> {
        return rootNodes.mapNotNull {
            it.allChildNodes()
        }.flatten()
    }
}
