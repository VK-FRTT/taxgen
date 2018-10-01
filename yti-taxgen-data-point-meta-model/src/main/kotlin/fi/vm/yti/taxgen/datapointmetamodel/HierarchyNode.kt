package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy

data class HierarchyNode(
    override val id: String,
    override val uri: String,
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

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

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

        //TODO: Validate memberRef ?
        validateConditionTruthy(
            validationResults = validationResults,
            instance = this,
            property = HierarchyNode::memberRef,
            condition = { memberRef.id.isNotEmpty() && memberRef.id.isNotBlank() },
            message = { "empty or blank ID" }
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
