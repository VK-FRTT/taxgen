package fi.vm.yti.taxgen.rddpmmapper.conceptitem

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.Member

internal data class ExplicitDomainItem(
    override val uri: String,
    val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>,
    val subCodeListUri: String?,
    val memberPrefix: String?,
    val order: Int
) : UriIdentifiedItem {

    fun toExplicitDomain(): ExplicitDomain {
        return ExplicitDomain(
            uri = uri,
            concept = concept,
            domainCode = domainCode,
            members = members,
            hierarchies = hierarchies
        )
    }
}
