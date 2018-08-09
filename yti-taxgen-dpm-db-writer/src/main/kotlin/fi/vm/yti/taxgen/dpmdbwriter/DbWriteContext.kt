package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.datapointmetamodel.Language
import fi.vm.yti.taxgen.datapointmetamodel.Owner
import org.jetbrains.exposed.dao.EntityID

data class DbWriteContext(
    val owner: Owner,
    val ownerId: EntityID<Int>,
    val languageIds: Map<Language, EntityID<Int>>
)
