package fi.vm.yti.taxgen.rdsprovider.configinput

import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.Owner

@Suppress("MemberVisibilityCanBePrivate")
data class OwnerConfigInput(
    val name: String?,
    val namespace: String?,
    val prefix: String?,
    val location: String?,
    val copyright: String?,
    val languages: List<String?>?
) {

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
