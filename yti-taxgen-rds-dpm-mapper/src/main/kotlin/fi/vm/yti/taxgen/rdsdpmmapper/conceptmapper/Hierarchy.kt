package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.ExtensionSourceReader

internal fun mapAndValidateHierarchies(
    codeListSource: CodeListSourceReader,
    acceptedExtensionTypes: List<RdsExtensionType>,
    owner: Owner,
    diagnostic: Diagnostic
): List<Hierarchy> {
    val hierarchies = mutableListOf<Hierarchy>()

    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (acceptedExtensionTypes.any { extensionMetadata.isType(it) }) {
            val rootNodes = mapAndValidateHierarchyNodes(
                extensionSource,
                owner,
                diagnostic
            )

            val hierarchy = Hierarchy(
                uri = extensionMetadata.validUri(diagnostic),
                concept = extensionMetadata.dpmConcept(owner),
                hierarchyCode = extensionMetadata.codeValueOrEmpty(),
                rootNodes = rootNodes
            )

            diagnostic.validate(hierarchy)
            hierarchies.add(hierarchy)
        }
    }

    hierarchies.sortWith(compareBy(NumberAwareStringComparator.instance()) { it.hierarchyCode })

    return hierarchies
}

private fun mapAndValidateHierarchyNodes(
    extensionSource: ExtensionSourceReader,
    owner: Owner,
    diagnostic: Diagnostic
): List<HierarchyNode> {
    val workingNodes = mutableListOf<HierarchyNodeItem>()

    extensionSource.eachExtensionMember { extensionMember ->

        val nodeItem = HierarchyNodeItem(
            uri = extensionMember.validUri(diagnostic),
            concept = extensionMember.dpmConcept(owner),
            comparisonOperator = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.ComparisonOperator),
            unaryOperator = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.UnaryOperator),
            referencedMemberUri = extensionMember.validCodeUri(diagnostic),
            parentMemberUri = extensionMember.relatedMember?.uri,
            order = extensionMember.validOrder(diagnostic)
        )

        workingNodes.add(nodeItem)
    }

    workingNodes.sortWith(compareBy { it.order })

    val rootNode = HierarchyNodeRoot()
    rootNode.buildTree(workingNodes)

    if (workingNodes.any()) {
        diagnostic.fatal("Extension has members which are not part of hierarchy")
    }

    return rootNode.createAndValidateHierarchyNodes(diagnostic)
}
