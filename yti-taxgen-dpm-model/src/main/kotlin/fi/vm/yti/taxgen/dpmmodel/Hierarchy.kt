package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Hierarchy(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Hierarchy::hierarchyCode,
            minLength = 2,
            maxLength = 50
        )

        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "rootNodes",
            iterable = allNodes(),
            valueSelector = { it.id },
            valueDescription = "id"
        )

        //TODO - better error message for this (as it is quite likely user caused modeling issue)
        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "rootNodes",
            iterable = allNodes(),
            valueSelector = { it.memberRef.id },
            valueDescription = "memberRef.id"
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.allNodes() }
            .flatten()
    }
}
