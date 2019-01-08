package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionType
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsMemberValueType
import fi.vm.yti.taxgen.rdsdpmmapper.sourcereader.CodeListSourceReader

internal fun mapAndValidateTypedDomains(
    codeListSource: CodeListSourceReader?,
    owner: Owner,
    diagnostic: Diagnostic
): List<TypedDomain> {
    val typedDomains = mutableListOf<TypedDomain>()

    if (codeListSource == null) return typedDomains

    //Base details
    codeListSource.eachCode { code ->
        val typedDomain = TypedDomain(
            uri = code.validUri(diagnostic),
            concept = code.dpmConcept(owner),
            domainCode = code.codeValueOrEmpty(),
            dataType = ""
        )

        typedDomains.add(typedDomain)
    }

    //Extension based details
    codeListSource.eachExtensionSource { extensionSource ->
        val extensionMetadata = extensionSource.extensionMetaData()

        if (extensionMetadata.isType(RdsExtensionType.DpmTypedDomain)) {

            extensionSource.eachExtensionMember { extensionMember ->
                val codeUri = extensionMember.validCodeUri(diagnostic)
                val typedDomain = typedDomains.find { it.uri == codeUri }

                if (typedDomain != null) {

                    val updatedTypedDomain = typedDomain.copy(
                        dataType = extensionMember.stringValueOrEmpty(RdsMemberValueType.DpmDomainDataType)
                    )

                    typedDomains.replaceOrAddByUri(updatedTypedDomain)
                }
            }
        }
    }

    diagnostic.validate(typedDomains)

    return typedDomains
}
