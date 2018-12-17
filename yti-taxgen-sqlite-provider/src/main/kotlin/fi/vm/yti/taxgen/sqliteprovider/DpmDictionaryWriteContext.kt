package fi.vm.yti.taxgen.sqliteprovider

import fi.vm.yti.taxgen.dpmmodel.Language
import fi.vm.yti.taxgen.dpmmodel.Owner
import org.jetbrains.exposed.dao.EntityID

data class DpmDictionaryWriteContext(
    val owner: Owner,
    val ownerId: EntityID<Int>,
    val languageIds: Map<Language, EntityID<Int>>
)
