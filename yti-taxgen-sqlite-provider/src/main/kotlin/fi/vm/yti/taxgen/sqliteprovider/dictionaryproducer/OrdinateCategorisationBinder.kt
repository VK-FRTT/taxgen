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
        private const val SIGNATURE_OPEN_MEMBER_MARKER = "*"

        fun rememberInitialCategorizations(diagnostic: Diagnostic): OrdinateCategorisationBinder {

            fun parseSignature(signature: String?): Pair<String, String> {
                signature ?: diagnostic.fatal("Empty DimensionMemberSignature")

                val match = DIMENSION_MEMBER_SIGNATURE_PATTERN.matchEntire(signature)
                    ?: diagnostic.fatal("Unsupported DimensionMemberSignature pattern")

                return match.groupValues[1] to match.groupValues[2]
            }

            val categorisations = transaction {
                OrdinateCategorisationTable
                    .selectAll()
                    .map { row ->
                        val signature = row[OrdinateCategorisationTable.dimensionMemberSignatureCol]
                        val (dimensionPart, memberPart) = parseSignature(signature)

                        OrdinateCategorisationBindingData(
                            ordinateId = row[OrdinateCategorisationTable.ordinateIdCol],
                            dimensionMemberSignature = signature,
                            signatureDimensionPart = dimensionPart,
                            signatureMemberPart = memberPart,
                            source = row[OrdinateCategorisationTable.sourceCol],
                            dps = row[OrdinateCategorisationTable.dpsCol]
                        )
                    }
            }

            return OrdinateCategorisationBinder(categorisations, diagnostic)
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
            val dimensionId = dimensionIds[categorisationItem.signatureDimensionPart]

            val memberId = if (categorisationItem.signatureMemberPart == SIGNATURE_OPEN_MEMBER_MARKER) {
                fixedEntitiesLookupItem.openMemberId
            } else {
                memberIds[categorisationItem.signatureMemberPart]
            }

            categorisationItem.copy(
                dimensionId = dimensionId,
                memberId = memberId
            )
        }

        reboundCategorisations.forEach {
            val info = ValidatableInfo(
                objectKind = "OrdinateCategorisation",
                objectAddress = "OrdinateID: ${it.ordinateId}, DimensionMemberSignature: ${it.dimensionMemberSignature}"
            )

            diagnostic.validate(it, info)
        }

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
