package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.validateCondition

data class HierarchyNode(
    override val id: String,
    override val concept: Concept,
    val abstract: Boolean,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val memberRef: DpmElementRef,
    val childNodes: List<HierarchyNode>?
) : DpmElement {

    companion object {
        val VALID_COMPARISON_OPERATORS = listOf("=", "<=", ">=", null)
        val VALID_UNARY_OPERATORS = listOf("+", "-", null)
    }

    override fun validate(validationErrors: ValidationErrors) {

        super.validate(validationErrors)

        validateCondition(
            validationErrors = validationErrors,
            instance = this,
            property = HierarchyNode::comparisonOperator,
            condition = {
                !VALID_COMPARISON_OPERATORS.contains(comparisonOperator)
            },
            failMessage = { "unsupported arithmetical relationship (comparison operator) '$comparisonOperator'" }
        )

        validateCondition(
            validationErrors = validationErrors,
            instance = this,
            property = HierarchyNode::unaryOperator,
            condition = {
                !VALID_UNARY_OPERATORS.contains(unaryOperator)
            },
            failMessage = { "unsupported arithmetical sign (unary operator) '$unaryOperator'" }
        )
    }

    fun allNodes(): List<HierarchyNode> = mutableListOf(this)
        .also {
            if (childNodes != null) {
                it.addAll(childNodes
                    .map { it.allNodes() }
                    .flatten()
                )
            }
        }
}
