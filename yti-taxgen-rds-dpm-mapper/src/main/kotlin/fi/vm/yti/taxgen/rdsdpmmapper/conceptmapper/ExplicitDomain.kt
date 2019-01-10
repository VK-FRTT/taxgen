package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.naturalsort.NumberAwareStringComparator
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.dpmElementRef
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.ExtensionSourceReader

internal fun mapAndValidateExplicitDomainsAndHierarchies(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<ExplicitDomain> {
    val explicitDomainItems = mutableListOf<ExplicitDomainItem>()

    if (codeListSource == null) return emptyList()

    //Base details
    codeListSource.eachCode { code ->
        val domain = ExplicitDomainItem(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            domainCode = code.codeValueOrEmpty(),
            members = emptyList(),
            hierarchies = emptyList(),
            subCodeListUri = code.subCodeScheme?.uri,
            memberPrefix = null,
            order = code.validOrder(diagnostic)
        )

        explicitDomainItems.add(domain)
    }

    //Extension based details
    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmExplicitDomain)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val domain = explicitDomainItems.find { it.uri == codeUri }

                if (domain != null) {
                    val updatedDomain = domain.copy(
                        memberPrefix = extensionMember.stringValueOrNull(RdsMemberValueType.DpmMemberXBRLCodePrefix)
                    )

                    explicitDomainItems.replaceOrAddByUri(updatedDomain)
                }
            }
        }
    }

    //SubCodeList based details
    codeListSource.eachSubCodeListSource { subCodeListSource ->
        val subCodeListUri = subCodeListSource.codeListMeta().validUri(diagnostic)
        val domain = explicitDomainItems.find { it.subCodeListUri == subCodeListUri }

        if (domain != null) {
            val members = mapAndValidateExplicitDomainMembers(
                subCodeListSource,
                owner,
                domain.memberPrefix,
                diagnostic
            )

            val hierarchies = mapAndValidateExplicitDomainHierarchies(
                subCodeListSource,
                owner,
                diagnostic
            )

            val updatedDomain = domain.copy(
                members = members,
                hierarchies = hierarchies
            )

            explicitDomainItems.replaceOrAddByUri(updatedDomain)
        }
    }

    val explicitDomains = explicitDomainItems
        .apply { sortWith(compareBy { it.order }) }
        .map { it.toExplicitDomain() }

    validateDpmElements(diagnostic, explicitDomains)

    return explicitDomains
}

private fun mapAndValidateExplicitDomainMembers(
    codeListSource: CodeListSourceReader,
    owner: Owner,
    memberPrefix: String?,
    diagnostic: Diagnostic
): List<Member> {
    val memberItems = mutableListOf<MemberItem>()

    val subCodeListMeta = codeListSource.codeListMeta()
    val defaultCodeUri = subCodeListMeta.defaultCode?.uri

    codeListSource.eachCode { code ->
        val member = MemberItem(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            memberCode = "${memberPrefix ?: ""}${code.codeValueOrEmpty()}",
            defaultMember = code.hasUri(defaultCodeUri),
            order = code.validOrder(diagnostic)
        )

        memberItems.add(member)
    }

    val members = memberItems
        .apply { sortWith(compareBy { it.order }) }
        .map { it.toMember() }

    validateDpmElements(diagnostic, members)

    return members
}

private fun mapAndValidateExplicitDomainHierarchies(
    codeListSource: CodeListSourceReader,
    owner: Owner,
    diagnostic: Diagnostic
): List<Hierarchy> {
    val hierarchies = mutableListOf<Hierarchy>()

    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DefinitionHierarchy) ||
            extensionMetadata.isType(RdsExtensionType.CalculationHierarchy)
        ) {
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

        val domainMemberRef = if (extensionMember.code == null) {
            dpmElementRef<Member>("", "${extensionMember.diagnosticLabel()}: No Code reference")
        } else {
            dpmElementRef<Member>(
                extensionMember.code.validUri(diagnostic),
                extensionMember.code.diagnosticLabel()
            )
        }

        val nodeItem = HierarchyNodeItem(
            uri = extensionMember.validUri(diagnostic),
            concept = extensionMember.dpmConcept(owner),
            comparisonOperator = extensionMember.stringValueOrNull(RdsMemberValueType.ComparisonOperator),
            unaryOperator = extensionMember.stringValueOrNull(RdsMemberValueType.UnaryOperator),
            memberRef = domainMemberRef,
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
