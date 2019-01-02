package fi.vm.yti.taxgen.rdsdpmmapper.mapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.HierarchyNode
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.dpmElementRef
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMember
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.ExtensionSourceReader

private data class ExplicitDomainItem(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>,
    val subCodeListUri: String?,
    val memberPrefix: String?
) : DpmElement {

    fun toExplicitDomain(): ExplicitDomain {
        return ExplicitDomain(
            id = id,
            uri = uri,
            concept = concept,
            domainCode = domainCode,
            members = members,
            hierarchies = hierarchies
        )
    }
}

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
            id = code.idOrEmpty(),
            concept = code.dpmConcept(owner),
            domainCode = code.codeValueOrEmpty(),
            members = emptyList(),
            hierarchies = emptyList(),
            subCodeListUri = code.subCodeListUriOrNull(),
            memberPrefix = null
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

    val explicitDomains = explicitDomainItems.map { it.toExplicitDomain() }

    diagnostic.validate(explicitDomains)

    return explicitDomains
}


private fun mapAndValidateExplicitDomainMembers(
    codeListSource: CodeListSourceReader,
    owner: Owner,
    memberPrefix: String?,
    diagnostic: Diagnostic
): List<Member> {
    val members = mutableListOf<Member>()

    val subCodeListMeta = codeListSource.codeListMeta()
    val defaultCodeUri = subCodeListMeta.defaultCode?.uri

    codeListSource.eachCode { code ->
        val member = Member(
            uri = code.validUri(diagnostic),
            id = code.idOrEmpty(),
            concept = code.dpmConcept(owner),
            memberCode = "${memberPrefix ?: ""}${code.codeValueOrEmpty()}",
            defaultMember = code.hasUri(defaultCodeUri)
        )

        members.add(member)
    }

    diagnostic.validate(members)

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
                id = extensionMetadata.idOrEmpty(),
                uri = extensionMetadata.validUri(diagnostic),
                concept = extensionMetadata.dpmConcept(owner),
                hierarchyCode = extensionMetadata.codeValueOrEmpty(),
                rootNodes = rootNodes
            )

            diagnostic.validate(hierarchy)
            hierarchies.add(hierarchy)
        }
    }

    return hierarchies
}

private fun mapAndValidateHierarchyNodes(
    extensionSource: ExtensionSourceReader,
    owner: Owner,
    diagnostic: Diagnostic
): List<HierarchyNode> {

    fun createNode(
        extensionMember: RdsExtensionMember,
        childNodes: List<HierarchyNode>
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

        return HierarchyNode(
            id = extensionMember.idOrEmpty(),
            uri = extensionMember.validUri(diagnostic),
            concept = extensionMember.dpmConcept(owner),
            abstract = false,
            comparisonOperator = extensionMember.stringValueOrNull(RdsMemberValueType.ComparisonOperator),
            unaryOperator = extensionMember.stringValueOrNull(RdsMemberValueType.UnaryOperator),
            memberRef = memberRef,
            childNodes = childNodes
        )
    }

    fun collectChildNodes(
        parentMember: RdsExtensionMember,
        allExtensionMembers: List<RdsExtensionMember>
    ): List<HierarchyNode> {
        val childNodes = allExtensionMembers
            .filter { extensionMember -> extensionMember.isChildOf(parentMember) }
            .map { extensionMember ->

                val childNodes = collectChildNodes(extensionMember, allExtensionMembers)

                createNode(extensionMember, childNodes)
            }

        return childNodes
    }

    val allExtensionMembers = extensionSource.extensionMembers()

    val rootNodes = allExtensionMembers
        .filter { extensionMember -> extensionMember.isRootMember() }
        .map { extensionMember ->
            val childNodes = collectChildNodes(extensionMember, allExtensionMembers)
            createNode(extensionMember, childNodes)
        }

    return rootNodes
}



