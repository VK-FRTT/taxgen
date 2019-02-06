package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementValueUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Hierarchy(
    override val uri: String,
    override val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(validationResults)

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
            valueSelector = { it.uri },
            valueDescription = "uri"
        )

        //TODO - better error message for this (as it is quite likely user caused modeling issue)
        validateElementValueUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "rootNodes",
            iterable = allNodes(),
            valueSelector = { it.referencedMemberUri },
            valueDescription = "referencedMemberUri"
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.allNodes() }
            .flatten()
    }

    fun traverseNodesInPreOrder(
        action: (parentNode: HierarchyNode?, currentNode: HierarchyNode, currentLevel: Int) -> Unit
    ) {
        rootNodes.forEach {
            it.traverseInPreOrder(null, 1, action)
        }
    }
}
