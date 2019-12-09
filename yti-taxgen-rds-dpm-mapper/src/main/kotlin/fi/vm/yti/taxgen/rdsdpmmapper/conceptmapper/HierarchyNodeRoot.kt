package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.rdsdpmmapper.conceptitem.HierarchyNodeItem

internal data class HierarchyNodeRoot(
    private val children: MutableList<HierarchyNodeItem> = mutableListOf()
) {
    fun buildTree(candidateNodes: MutableList<HierarchyNodeItem>) {

        candidateNodes
            .filter { node -> node.isRootMember() }
            .also {
                children.addAll(it)
                candidateNodes.removeAll(it)
            }

        children.forEach { it.takeChildren(candidateNodes) }
    }

    fun createAndValidateHierarchyNodes(
        diagnostic: Diagnostic
    ): List<HierarchyNode> = children.map { it.createAndValidateHierarchyNode(diagnostic) }
}
