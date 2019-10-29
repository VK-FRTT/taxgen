package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsdpmmapper.conceptitem.HierarchyNodeItem
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.modelmapper.CodeListModelMapper
import fi.vm.yti.taxgen.rdsdpmmapper.modelmapper.ExtensionModelMapper

internal fun mapAndValidateHierarchies(
    codeListSource: CodeListModelMapper,
    acceptedExtensionTypes: List<RdsExtensionType>,
    owner: Owner,
    elementCodesByUri: Map<String, String>,
    diagnostic: Diagnostic
): List<Hierarchy> {
    val hierarchies = mutableListOf<Hierarchy>()

    codeListSource.eachExtensionModelMapper { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (acceptedExtensionTypes.any { extensionMetadata.isType(it) }) {
            val rootNodes = mapAndValidateHierarchyNodes(
                extensionSource,
                owner,
                elementCodesByUri,
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
    extensionSource: ExtensionModelMapper,
    owner: Owner,
    elementCodesByUri: Map<String, String>,
    diagnostic: Diagnostic
): List<HierarchyNode> {

    val candidateNodes = mutableListOf<HierarchyNodeItem>()

    extensionSource.eachExtensionMember { extensionMember ->

        val extensionMemberUri = extensionMember.validUri(diagnostic)
        val extensionMemberCodeUri = extensionMember.validCodeUri(diagnostic)
        val referencedElementCode = elementCodesByUri[extensionMemberCodeUri] ?: ""

        val nodeItem = HierarchyNodeItem(
            uri = extensionMemberUri,
            concept = extensionMember.dpmConcept(owner),
            comparisonOperator = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.ComparisonOperator),
            unaryOperator = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.UnaryOperator),
            referencedElementCode = referencedElementCode,
            parentMemberUri = extensionMember.relatedMember?.uri,
            order = extensionMember.validOrder(diagnostic)
        )

        candidateNodes.add(nodeItem)
    }

    candidateNodes.sortWith(compareBy { it.order })

    val rootNode = HierarchyNodeRoot()
    rootNode.buildTree(candidateNodes)

    //Normally there should be no leftover nodes from tree building
    //However, RDS API responses have contained occasionally faulty parent references and thus tree building has failed
    if (candidateNodes.any()) {
        val orphanNodeUris = candidateNodes.map { "${it.uri} (${it.concept.label.translationWithPostixForAnyLangOrNull(owner.languages)})" }

        diagnostic.fatal("Corrupted source data. Codelist Extension has Members, which position in DPM Hierarchy could not be determined: ${orphanNodeUris.joinToString()}")
    }

    return rootNode.createAndValidateHierarchyNodes(diagnostic)
}
