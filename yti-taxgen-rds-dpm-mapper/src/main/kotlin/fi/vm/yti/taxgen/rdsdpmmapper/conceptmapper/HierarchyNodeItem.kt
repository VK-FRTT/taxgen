package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode

internal data class HierarchyNodeItem(
    val uri: String,
    val concept: Concept,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val referencedMemberUri: String,
    val parentMemberUri: String?,
    val order: Int,
    private val children: MutableList<HierarchyNodeItem> = mutableListOf()
) {

    fun isRootMember() = parentMemberUri == null

    fun isChildOf(other: HierarchyNodeItem): Boolean {
        if (parentMemberUri == null) return false

        return parentMemberUri == other.uri
    }

    fun takeChildren(workingNodes: MutableList<HierarchyNodeItem>) {

        workingNodes
            .filter { node -> node.isChildOf(this) }
            .also {
                children.addAll(it)
                workingNodes.removeAll(it)
            }

        children.forEach { it.takeChildren(workingNodes) }
    }

    fun createAndValidateHierarchyNode(
        diagnostic: Diagnostic
    ): HierarchyNode {
        val node = HierarchyNode(
            uri = uri,
            concept = concept,
            abstract = false,
            comparisonOperator = comparisonOperator,
            unaryOperator = unaryOperator,
            referencedMemberUri = referencedMemberUri,
            childNodes = children.map { it.createAndValidateHierarchyNode(diagnostic) }
        )

        diagnostic.validate(node)

        return node
    }
}
