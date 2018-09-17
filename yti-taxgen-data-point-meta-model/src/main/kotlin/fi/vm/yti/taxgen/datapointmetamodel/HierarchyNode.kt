package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.customValidate

data class HierarchyNode(
    val concept: Concept,
    val abstract: Boolean,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val member: Member,
    val childNodes: List<HierarchyNode>?
) : Validatable {

    companion object {
        val VALID_COMPARISON_OPERATORS = listOf("=", "<=", ">=", null)
        val VALID_UNARY_OPERATORS = listOf("+", "-", null)
    }

    override fun validate(validationErrors: ValidationErrors) {

        concept.validate(validationErrors)

        customValidate(
            validationErrors = validationErrors,
            instance = this,
            property = HierarchyNode::comparisonOperator,
            failIf = {
                !VALID_COMPARISON_OPERATORS.contains(comparisonOperator)
            },
            failMsg = { "unsupported arithmetical relationship (comparison operator) '$comparisonOperator'" }
        )

        customValidate(
            validationErrors = validationErrors,
            instance = this,
            property = HierarchyNode::unaryOperator,
            failIf = {
                !VALID_UNARY_OPERATORS.contains(unaryOperator)
            },
            failMsg = { "unsupported arithmetical sign (unary operator) '$unaryOperator'" }
        )
    }

    fun allChildNodes(): List<HierarchyNode>? {
        if (childNodes == null) return listOf(this)

        return childNodes.mapNotNull {
            it.allChildNodes()
        }.flatten().toMutableList().also { it.add(0, this) }
    }
}
