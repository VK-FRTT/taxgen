package fi.vm.yti.taxgen.sqliteprovider.tables

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object Tables {
    fun create() {
        transaction {
            SchemaUtils.create(
                ConceptTable,
                ConceptTranslationTable,
                LanguageTable,
                OwnerTable,
                DomainTable,
                MemberTable,
                HierarchyTable,
                HierarchyNodeTable
            )
        }
    }
}
