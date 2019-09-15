package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.sqliteprovider.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    val databaseIdSignature: OrdinateCategorisationSignature,
    val xbrlCodeSignature: OrdinateCategorisationSignature,

    val source: String?
) : Validatable {

    companion object {

        fun fromRow(
            row: ResultRow,
            diagnostic: Diagnostic
        ): BaselineOrdinateCategorisation {

            val databaseIdSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dimensionMemberSignatureCol,
                OrdinateCategorisationSignature.Type.DATABASE_ID_SIGNATURE,
                diagnostic
            )

            val xbrlCodeSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dpsCol,
                OrdinateCategorisationSignature.Type.XBRL_CODE_SIGNATURE,
                diagnostic
            )

            return BaselineOrdinateCategorisation(
                ordinateId = row[OrdinateCategorisationTable.ordinateIdCol],
                databaseIdSignature = databaseIdSignature,
                xbrlCodeSignature = xbrlCodeSignature,
                source = row[OrdinateCategorisationTable.sourceCol]
            )
        }

        private fun tokenizeSignature(
            row: ResultRow,
            column: Column<String?>,
            type: OrdinateCategorisationSignature.Type,
            diagnostic: Diagnostic
        ): OrdinateCategorisationSignature {
            val signatureLiteral = row[column] ?: diagnostic.fatal("Empty OrdinateCategorisation signature")

            val signatureMatch = SIGNATURE_PATTERN.matchEntire(signatureLiteral)
                ?: diagnostic.fatal("Unsupported signature in OrdinateCategorisation.${column.name}: $signatureLiteral")

            fun optionalSignatureElement(partName: String) =
                (signatureMatch.groups as MatchNamedGroupCollection)[partName]?.value

            fun mandatorySignatureElement(partName: String) = optionalSignatureElement(partName) ?: thisShouldNeverHappen("SignaturePattern configuration mismatch")

            return when {
                optionalSignatureElement("dimension") != null -> {
                    OrdinateCategorisationSignature(
                        type = type,
                        dimensionIdentifier = mandatorySignatureElement("dimension"),
                        memberIdentifier = mandatorySignatureElement("member"),
                        openAxisValueRestrictionSignature = null
                    )
                }

                (optionalSignatureElement("oaDimension") != null) -> {
                    OrdinateCategorisationSignature(
                        type = type,
                        dimensionIdentifier = mandatorySignatureElement("oaDimension"),
                        memberIdentifier = mandatorySignatureElement("oaMember"),
                        openAxisValueRestrictionSignature = OpenAxisValueRestrictionSignature(
                            hierarchyIdentifier = mandatorySignatureElement("oaHierarchy"),
                            hierarchyStartingMemberIdentifier = mandatorySignatureElement("oaStartMember"),
                            startingMemberIncluded = mandatorySignatureElement("oaStartMemberIncluded")
                        )
                    )
                }

                else -> {
                    thisShouldNeverHappen("Signature matching mismatch.")
                }
            }
        }

        private val SIGNATURE_PATTERN =
            """
            \A

            (?<dimension>[^\(\)]+)
                \(
                (?<member>[^\(\)\[\]]+)
                \)

            |

            (?<oaDimension>[^\(\)]+)
                \(
                (?<oaMember>[^\(\)\[\]]+)
                    \[
                    (?<oaHierarchy>[^\(\)\[\];]+)
                    ;
                    (?<oaStartMember>[^\(\)\[\];]+)
                    ;
                    (?<oaStartMemberIncluded>[^\(\)\[\];]+)
                    \]
                \)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)
    }

    override fun validate(validationResults: ValidationResults) {
        databaseIdSignature.validate(validationResults)
        xbrlCodeSignature.validate(validationResults)

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "signatures",
            validate = { messages ->
                val mismatchDescriptions = emptyList<String>() //checkSignaturesMatching()

                if (mismatchDescriptions.any()) {
                    messages.add(
                        "OrdinateCategorisation signatures do not match. ${mismatchDescriptions.joinToString()}"
                    )
                }
            }
        )
    }

    private fun checkSignaturesMatching(): List<String> {
        val descriptions = mutableListOf<String>()

        if (databaseIdSignature.memberIdentifier != xbrlCodeSignature.memberIdentifier) {
            descriptions.add("Members not same")
        }

        if (databaseIdSignature.dimensionIdentifier != xbrlCodeSignature.dimensionIdentifier) {
            descriptions.add("Dimensions not same")
        }

        if (databaseIdSignature.hasOpenAxisValueRestrictionSignature() xor
            xbrlCodeSignature.hasOpenAxisValueRestrictionSignature()
        ) {
            descriptions.add("OpenAxisValueRestriction parts not matching")
        }

        if (databaseIdSignature.openAxisValueRestrictionSignature != null &&
            xbrlCodeSignature.openAxisValueRestrictionSignature != null
        ) {
            val xbrlCodeDbReferences =
                OrdinateCategorisationDbReferences.fromOrdinateCategorisationXbrlCodeSignature(
                    xbrlCodeSignature
                )

            require(xbrlCodeDbReferences.openAxisValueRestrictionDbReferences != null)

            if (databaseIdSignature.openAxisValueRestrictionSignature.hierarchyIdentifier !=
                xbrlCodeDbReferences.openAxisValueRestrictionDbReferences.hierarchyId.toString()
            ) {
                descriptions.add("Hierarchies not same")
            }

            if (databaseIdSignature.openAxisValueRestrictionSignature.hierarchyStartingMemberIdentifier !=
                xbrlCodeDbReferences.openAxisValueRestrictionDbReferences.hierarchyStartingMemberId.toString()
            ) {
                descriptions.add("Hierarchy starting members not same")
            }

            if (databaseIdSignature.openAxisValueRestrictionSignature.startingMemberIncluded !=
                xbrlCodeSignature.openAxisValueRestrictionSignature.startingMemberIncluded
            ) {
                descriptions.add("Starting member inclusion not same")
            }
        }

        return descriptions
    }
}
