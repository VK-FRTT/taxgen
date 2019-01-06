package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode

internal data class HierarchyNodeRoot(
    private val children: MutableList<HierarchyNodeItem> = mutableListOf()
) {
    fun buildTree(workingNodes: MutableList<HierarchyNodeItem>) {

        workingNodes
            .filter { node -> node.isRootMember() }
            .also {
                children.addAll(it)
                workingNodes.removeAll(it)
            }

        children.forEach { it.takeChildren(workingNodes) }
    }

    fun createAndValidateHierarchyNodes(
        diagnostic: Diagnostic
    ): List<HierarchyNode> = children.map { it.createAndValidateHierarchyNode(diagnostic) }
}
