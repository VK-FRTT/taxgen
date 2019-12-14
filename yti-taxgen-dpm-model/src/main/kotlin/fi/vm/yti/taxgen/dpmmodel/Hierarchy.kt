package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength

data class Hierarchy(
    override val uri: String,
    override val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : DpmElement {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::hierarchyCode,
            minLength = 2,
            maxLength = 50
        )

        val allNodes = allNodes()

        validateIterableValuesUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = allNodes,
            valueSelector = { it.uri },
            valueName = listOf(HierarchyNode::class, HierarchyNode::uri)
        )

        validateIterableValuesUnique(
            validationResultBuilder = validationResultBuilder,
            iterable = allNodes,
            valueSelector = { it.referencedElementCode },
            valueName = listOf(HierarchyNode::class, HierarchyNode::referencedElementCode)
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.nodeAndChildrenAsList() }
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
