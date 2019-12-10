package fi.vm.yti.taxgen.rddpmmapper.conceptitem

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic

internal data class HierarchyNodeItem(
    override val uri: String,
    val concept: Concept,
    val comparisonOperator: String?,
    val unaryOperator: String?,
    val referencedElementCode: String,
    val parentMemberUri: String?,
    val order: Int,
    private val children: MutableList<HierarchyNodeItem> = mutableListOf()
) : UriIdentifiedItem {

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
            referencedElementCode = referencedElementCode,
            childNodes = children.map { it.createAndValidateHierarchyNode(diagnostic) }
        )

        diagnostic.validate(node)

        return node
    }
}
