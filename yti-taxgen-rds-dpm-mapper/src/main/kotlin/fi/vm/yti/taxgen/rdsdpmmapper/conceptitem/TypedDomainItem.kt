package fi.vm.yti.taxgen.rdsdpmmapper.conceptitem

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.TypedDomain

internal data class TypedDomainItem(
    override val uri: String,
    val concept: Concept,
    val domainCode: String,
    val dataType: String
) : UriIdentifiedItem {

    fun toTypedDomain(): TypedDomain {
        return TypedDomain(
            uri = uri,
            concept = concept,
            domainCode = domainCode,
            dataType = dataType
        )
    }
}
