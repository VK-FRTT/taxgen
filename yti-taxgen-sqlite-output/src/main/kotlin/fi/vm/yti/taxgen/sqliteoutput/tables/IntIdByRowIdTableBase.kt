package fi.vm.yti.taxgen.sqliteoutput.tables

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

open class IntIdByRowIdTableBase(name: String = "", columnName: String = "id") : IdTable<Int>(name) {
    override val id: Column<EntityID<Int>> = integer(columnName).entityId()
    override val primaryKey by lazy { PrimaryKey(id) }
}
