package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.TypedDomain
import fi.vm.yti.taxgen.rdsdpmmapper.ext.kotlin.replaceOrAddByUri
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsExtensionMember
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
                        dataType = extensionMember.mappedDomainDataType()
                    )

                    typedDomains.replaceOrAddByUri(updatedTypedDomain)
                }
            }
        }
    }

    validateDpmElements(diagnostic, typedDomains)

    return typedDomains
}

private val RDS_DOMAIN_DATA_TYPE_TO_DPM = mapOf(
    "Enumeration" to "Enumeration/Code",
    "Boolean" to "Boolean",
    "Date" to "Date",
    "Integer" to "Integer",
    "Monetary" to "Monetary",
    "Percentage" to "Percent",
    "String" to "String",
    "Decimal" to "Decimal",
    "Lei" to "Lei",
    "Isin" to "Isin"
)

private fun RdsExtensionMember.mappedDomainDataType(): String {
    val sourceVal = stringValueOrEmpty(RdsMemberValueType.DpmDomainDataType)
    val mappedVal = RDS_DOMAIN_DATA_TYPE_TO_DPM[sourceVal]

    return mappedVal ?: sourceVal ?: ""
}
