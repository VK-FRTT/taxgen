package fi.vm.yti.taxgen.rdsource.configdata

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic

@Suppress("MemberVisibilityCanBePrivate")
data class OwnerConfigData(
    val name: String?,
    val namespace: String?,
    val prefix: String?,
    val location: String?,
    val copyright: String?,
    val languages: List<String?>?
) {
    companion object {
        fun fomOwner(owner: Owner): OwnerConfigData {

            return OwnerConfigData(
                name = owner.name,
                namespace = owner.namespace,
                prefix = owner.prefix,
                location = owner.location,
                copyright = owner.copyright,
                languages = owner.languageCodes
            )
        }
    }

    fun toOwner(diagnostic: Diagnostic): Owner {

        @Suppress("UNCHECKED_CAST")
        val owner = Owner(
            name = name ?: "",
            namespace = namespace ?: "",
            prefix = prefix ?: "",
            location = location ?: "",
            copyright = copyright ?: "",
            languageCodes = languages?.map { it ?: "" } ?: emptyList()
        )

        diagnostic.validate(owner)

        return owner
    }
}
