package fi.vm.yti.taxgen.sqliteprovider.conceptwriter

import fi.vm.yti.taxgen.dpmmodel.Owner
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.tables.OwnerTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.transactions.transaction

object DbOwners {

    fun writeOwner(owner: Owner): EntityID<Int> {

        val ownerId = transaction {
            OwnerTable.insertOwner(owner)
        }

        return ownerId
    }

    fun lookupOwnerIdByPrefix(
        owner: Owner,
        diagnostic: Diagnostic
    ): EntityID<Int> {
        return transaction {
            val matchingRows = OwnerTable.rowsWhereOwnerPrefix(owner.prefix)

            if (matchingRows.count() != 1) {
                diagnostic.fatal("Selecting Owner from database failed. Found ${matchingRows.count()} Owners with prefix '${owner.prefix}'.")
            }

            matchingRows.first()[OwnerTable.id]
        }
    }
}
