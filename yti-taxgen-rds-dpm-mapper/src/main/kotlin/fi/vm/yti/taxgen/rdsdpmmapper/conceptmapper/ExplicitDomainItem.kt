package fi.vm.yti.taxgen.rdsdpmmapper.conceptmapper

import fi.vm.yti.taxgen.dpmmodel.Concept
import fi.vm.yti.taxgen.dpmmodel.DpmElement
import fi.vm.yti.taxgen.dpmmodel.ExplicitDomain
import fi.vm.yti.taxgen.dpmmodel.Hierarchy
import fi.vm.yti.taxgen.dpmmodel.Member

internal data class ExplicitDomainItem(
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
