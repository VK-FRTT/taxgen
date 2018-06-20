package fi.vm.yti.taxgen.dpmdbwriter.tables

import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

object Tables {
    fun create() {
        transaction {
            create(ConceptTable)
            create(ConceptTranslationTable)
            create(LanguageTable)
            create(OwnerTable)
        }
    }
}
