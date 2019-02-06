package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Member
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.rdsdpmmapper.conceptitem.ExplicitDomainItem
import fi.vm.yti.taxgen.rdsdpmmapper.conceptitem.MemberItem
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddItemByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader

internal fun mapAndValidateExplicitDomainsAndHierarchies(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<ExplicitDomain> {
    codeListSource ?: return emptyList()

    val explicitDomainItems = mutableListOf<ExplicitDomainItem>()

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
                        memberPrefix = extensionMember.nonEmptyStringValueOrNull(RdsMemberValueType.DpmMemberXBRLCodePrefix)
                    )

                    explicitDomainItems.replaceOrAddItemByUri(updatedDomain)
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

            val hierarchies = mapAndValidateHierarchies(
                subCodeListSource,
                listOf(RdsExtensionType.DefinitionHierarchy, RdsExtensionType.CalculationHierarchy),
                owner,
                diagnostic
            )

            val updatedDomain = domain.copy(
                members = members,
                hierarchies = hierarchies
            )

            explicitDomainItems.replaceOrAddItemByUri(updatedDomain)
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
