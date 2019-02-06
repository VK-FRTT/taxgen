package fi.vm.yti.taxgen.rdsdpmmapper.conceptitem

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.ExplicitDimension
import fi.vm.yti.taxgen.dpmmodel.TypedDimension

internal data class DimensionItem(
    override val uri: String,
    val concept: Concept,
    val dimensionCode: String,
    val referencedDomainCode: String
) : UriIdentifiedItem {

    fun toExplicitDimension(): ExplicitDimension {
        return ExplicitDimension(
            uri = uri,
            concept = concept,
            dimensionCode = dimensionCode,
            referencedDomainCode = referencedDomainCode
        )
    }

    fun toTypedDimension(): TypedDimension {
        return TypedDimension(
            uri = uri,
            concept = concept,
            dimensionCode = dimensionCode,
            referencedDomainCode = referencedDomainCode
        )
    }
}
