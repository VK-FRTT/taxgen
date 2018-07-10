package fi.vm.yti.taxgen.dpmdbwriter

import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.ConceptTranslationTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.DomainTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.LanguageTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.MemberTable
import fi.vm.yti.taxgen.dpmdbwriter.tables.OwnerTable
import org.jetbrains.exposed.sql.SchemaUtils.create
import org.jetbrains.exposed.sql.transactions.transaction

object DbTables {
    fun create() {
        transaction {
            create(ConceptTable)
            create(ConceptTranslationTable)
            create(LanguageTable)
            create(OwnerTable)
            create(DomainTable)
            create(MemberTable)
        }
    }
}
