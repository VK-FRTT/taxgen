package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.dpmElementRef
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType


internal data class HierarchyNodeItem(
    private val extensionMember: RdsExtensionMember,
    private val children: MutableList<HierarchyNodeItem> = mutableListOf()
) {
    companion object {
        fun root(): HierarchyNodeItem {
            val emptyMember = RdsExtensionMember(
                id = null,
                uri = null,
                codeValue = null,
                created = null,
                modified = null,
                startDate = null,
                endDate = null,
                prefLabel = null,
                description = null,
                code = null,
                memberValues = null,
                relatedMember = null
            )

            return HierarchyNodeItem(emptyMember)
        }
    }

    fun buildTree(workingNodes: MutableList<HierarchyNodeItem>) {

        workingNodes
            .filter { node -> node.extensionMember.isRootMember() }
            .also {
                children.addAll(it)
                workingNodes.removeAll(it)
            }

        children.forEach { it.takeChildren(workingNodes) }
    }


    private fun takeChildren(workingNodes: MutableList<HierarchyNodeItem>) {

        workingNodes
            .filter { node -> node.extensionMember.isChildOf(extensionMember) }
            .also {
                children.addAll(it)
                workingNodes.removeAll(it)
            }

        children.forEach { it.takeChildren(workingNodes) }
    }

    private fun createAndValidateHierarchyNode(
        owner: Owner,
        diagnostic: Diagnostic
    ): HierarchyNode {
        val memberRef = if (extensionMember.code == null) {
            dpmElementRef<Member>("", "", "${extensionMember.diagnosticLabel()}: No Code reference")
        } else {
            dpmElementRef<Member>(
                extensionMember.code.idOrEmpty(),
                extensionMember.code.validUri(diagnostic),
                extensionMember.code.diagnosticLabel()
            )
        }

        val node = HierarchyNode(
            id = extensionMember.idOrEmpty(),
            uri = extensionMember.validUri(diagnostic),
            concept = extensionMember.dpmConcept(owner),
            abstract = false,
            comparisonOperator = extensionMember.stringValueOrNull(RdsMemberValueType.ComparisonOperator),
            unaryOperator = extensionMember.stringValueOrNull(RdsMemberValueType.UnaryOperator),
            memberRef = memberRef,
            childNodes = createAndValidateHierarchyNodes(owner, diagnostic)
        )

        diagnostic.validate(node)

        return node
    }

    fun createAndValidateHierarchyNodes(
        owner: Owner,
        diagnostic: Diagnostic
    ): List<HierarchyNode> = children.map { it.createAndValidateHierarchyNode(owner, diagnostic) }
}
