package fi.vm.yti.taxgen.dpmdbwriter.writers

import fi.vm.yti.taxgen.datapointmetamodel.Owner
import fi.vm.yti.taxgen.dpmdbwriter.tables.OwnerTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object DbOwners {

    fun writeOwner(owner: Owner): EntityID<Int> {

        val ownerId = transaction {

            OwnerTable.insertAndGetId {
                it[ownerNameCol] = owner.name
                it[ownerNamespaceCol] = owner.namespace
                it[ownerLocationCol] = owner.location
                it[ownerPrefixCol] = owner.prefix
                it[ownerCopyrightCol] = owner.copyright

                it[parentOwnerIdCol] = null
                it[conceptIdCol] = null
            }
        }

        return ownerId
    }
}
