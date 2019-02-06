package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class HierarchyNode(
    override val uri: String,
    override val concept: Concept,
    val abstract: Boolean,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val referencedMemberUri: String, //Plain Member.memberCode can't be used in binding, as there can be duplicate memberCodes
    val childNodes: List<HierarchyNode>
) : DpmElement {

    companion object {
        val VALID_COMPARISON_OPERATORS = listOf("=", "<=", ">=", "<", ">", null)
        val VALID_UNARY_OPERATORS = listOf("+", "-", null)
    }

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(
            validationResults = validationResults,
            minLabelLangCount = 0
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = HierarchyNode::comparisonOperator,
            condition = { VALID_COMPARISON_OPERATORS.contains(comparisonOperator) },
            message = { "unsupported arithmetical relationship (comparison operator) '$comparisonOperator'" }
        )

        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = HierarchyNode::unaryOperator,
            condition = { VALID_UNARY_OPERATORS.contains(unaryOperator) },
            message = { "unsupported arithmetical sign (unary operator) '$unaryOperator'" }
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = HierarchyNode::referencedMemberUri,
            minLength = 1,
            maxLength = 500
        )
    }

    fun allNodes(): List<HierarchyNode> = mutableListOf(this)
        .also { nodes ->
            nodes.addAll(
                childNodes
                    .map { it.allNodes() }
                    .flatten()
            )
        }

    fun traverseInPreOrder(
        theParent: HierarchyNode?,
        theCurrentLevel: Int,
        action: (parentNode: HierarchyNode?, currentNode: HierarchyNode, levelIndex: Int) -> Unit
    ) {
        action(theParent, this, theCurrentLevel)

        childNodes.forEach {
            it.traverseInPreOrder(this, theCurrentLevel + 1, action)
        }
    }
}
