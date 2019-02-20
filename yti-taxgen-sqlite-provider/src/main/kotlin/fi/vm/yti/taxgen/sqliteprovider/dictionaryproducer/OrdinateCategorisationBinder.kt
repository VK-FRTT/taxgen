package fi.vm.yti.taxgen.sqliteprovider.dictionaryproducer

import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.DpmDictionaryLookupItem
import fi.vm.yti.taxgen.sqliteprovider.lookupitem.FixedEntitiesLookupItem
import fi.vm.yti.taxgen.sqliteprovider.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class OrdinateCategorisationBinder(
    val categorisationBindings: List<OrdinateCategorisationBindingData>,
    val diagnostic: Diagnostic
) {
    companion object {
        private val DIMENSION_MEMBER_SIGNATURE_PATTERN = "\\A([^\\(\\)]+)\\(([^\\(\\)]+)\\)\\z".toRegex()
        private const val OPEN_MEMBER_MARKER = "*"

        fun rememberInitialCategorizations(diagnostic: Diagnostic): OrdinateCategorisationBinder {

            fun tokenizeDps(dps: String?): List<String> {
                dps ?: diagnostic.fatal("Empty DPS")

                val match = DIMENSION_MEMBER_SIGNATURE_PATTERN.matchEntire(dps)
                    ?: diagnostic.fatal("Unsupported DPS structure")

                return listOf(match.groupValues[1], match.groupValues[2])
            }

            val categorisationBindings = transaction {
                OrdinateCategorisationTable
                    .selectAll()
                    .map { row ->
                        val dps = row[OrdinateCategorisationTable.dpsCol]
                        val (dimensionXbrlCode, memberXbrlCode) = tokenizeDps(dps)

                        OrdinateCategorisationBindingData(
                            ordinateId = row[OrdinateCategorisationTable.ordinateIdCol],
                            dimensionMemberSignature = row[OrdinateCategorisationTable.dimensionMemberSignatureCol],
                            source = row[OrdinateCategorisationTable.sourceCol],
                            dps = row[OrdinateCategorisationTable.dpsCol],

                            dpsDimensionXbrlCode = dimensionXbrlCode,
                            dpsMemberXbrlCode = memberXbrlCode,

                            dimensionId = null,
                            memberId = null
                        )
                    }
            }

            return OrdinateCategorisationBinder(categorisationBindings, diagnostic)
        }
    }

    fun rebindAndWriteCategorisations(
        dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>,
        fixedEntitiesLookupItem: FixedEntitiesLookupItem
    ) {
        //Rebinding can be based on XBRL code since it contains owner prefix & thus is unique DB wide
        // - Dimension XBRL Code = {owner.prefix}_dim:{dimension.dimensionCode}
        // - Member XBRL Code = {owner.prefix}_{domain.domainCode}:{member.memberCode}

        val dimensionIds = collectDimensionIdsByXbrlCode(dpmDictionaryLookupItems)
        val memberIds = collectMemberIdsByXbrlCode(dpmDictionaryLookupItems)

        val reboundCategorisations = categorisationBindings.map { categorisationItem ->
            val dimensionId = dimensionIds[categorisationItem.dpsDimensionXbrlCode]

            val memberId = if (categorisationItem.dpsMemberXbrlCode == OPEN_MEMBER_MARKER) {
                fixedEntitiesLookupItem.openMemberId
            } else {
                memberIds[categorisationItem.dpsMemberXbrlCode]
            }

            categorisationItem.copy(
                dimensionId = dimensionId,
                memberId = memberId
            )
        }

        reboundCategorisations.forEach {
            val info = ValidatableInfo(
                objectKind = "OrdinateCategorisation",
                objectAddress = "OrdinateID: ${it.ordinateId}, DPS: ${it.dps}"
            )

            diagnostic.validate(it, info)
        } //TODO - fail if having errors?

        transaction {
            OrdinateCategorisationTable.deleteAll()
        }

        transaction {
            reboundCategorisations.forEach(::insertOrdinateCategorisation)
        }
    }

    private fun collectDimensionIdsByXbrlCode(
        dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>
    ): Map<String, EntityID<Int>> {

        return dpmDictionaryLookupItems.flatMap { dictionaryItem ->
            dictionaryItem.dimensionLookupItems.map { dimensionItem ->
                dimensionItem.dimensionXbrlCode to dimensionItem.dimensionId
            }
        }.toMap()
    }

    private fun collectMemberIdsByXbrlCode(
        dpmDictionaryLookupItems: List<DpmDictionaryLookupItem>
    ): Map<String, EntityID<Int>> {

        return dpmDictionaryLookupItems.flatMap { dictionaryItem ->
            dictionaryItem.domainLookupItems.flatMap { domainItem ->
                domainItem.memberLookupItems.map { memberItem ->
                    memberItem.memberXbrlCode to memberItem.memberId
                }
            }
        }.toMap()
    }

    private fun insertOrdinateCategorisation(categorisationBinding: OrdinateCategorisationBindingData) {
        OrdinateCategorisationTable.insert {
            it[ordinateIdCol] = categorisationBinding.ordinateId
            it[dimensionIdCol] = categorisationBinding.dimensionId
            it[memberIdCol] = categorisationBinding.memberId
            it[dimensionMemberSignatureCol] = categorisationBinding.dimensionMemberSignature
            it[sourceCol] = categorisationBinding.source
            it[dpsCol] = categorisationBinding.dps
        }
    }
}
