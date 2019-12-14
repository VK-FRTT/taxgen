package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropFulfillsCondition
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength

data class HierarchyNode(
    override val uri: String,
    override val concept: Concept,
    val abstract: Boolean,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val referencedElementCode: String,
    val childNodes: List<HierarchyNode>
) : DpmElement {

    companion object {
        val VALID_COMPARISON_OPERATORS = listOf("=", "<=", ">=", "<", ">", null)
        val VALID_UNARY_OPERATORS = listOf("+", "-", null)
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(
            validationResultBuilder = validationResultBuilder,
            minLabelLangCount = 0
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::comparisonOperator,
            condition = { VALID_COMPARISON_OPERATORS.contains(it) },
            reason = { "Unsupported arithmetical relationship" }
        )

        validatePropFulfillsCondition(
            validationResultBuilder = validationResultBuilder,
            property = this::unaryOperator,
            condition = { VALID_UNARY_OPERATORS.contains(it) },
            reason = { "Unsupported arithmetical sign" }
        )

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::referencedElementCode,
            minLength = 1,
            maxLength = 50
        )
    }

    fun nodeAndChildrenAsList(): List<HierarchyNode> =
        listOf(this) + childNodes.map { it.nodeAndChildrenAsList() }.flatten()

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
