package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateConditionTruthy
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonBlank
import fi.vm.yti.taxgen.sqliteprovider.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    val databaseIdSignature: BaselineOrdinateCategorisation.Signature,
    val xbrlCodeSignature: BaselineOrdinateCategorisation.Signature,

    val source: String?
) : Validatable {

    data class Signature(
        val dimensionIdentifier: String,
        val memberIdentifier: String,
        val openAxisValueRestrictionSignature: OpenAxisValueRestrictionSignature?
    ) {
        fun validate(validationResults: ValidationResults) {
            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = Signature::dimensionIdentifier
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = Signature::memberIdentifier
            )

            openAxisValueRestrictionSignature?.validate(validationResults)
        }
    }

    data class OpenAxisValueRestrictionSignature(
        val hierarchyIdentifier: String,
        val hierarchyStartingMemberIdentifier: String,
        val startingMemberIncluded: String
    ) {
        companion object {
            val VALID_STARTING_MEMBER_INCLUDED_VALUES = listOf("0", "1")
        }

        fun validate(validationResults: ValidationResults) {
            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionSignature::hierarchyIdentifier
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionSignature::hierarchyStartingMemberIdentifier
            )

            validateNonBlank(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionSignature::startingMemberIncluded
            )

            validateConditionTruthy(
                validationResults = validationResults,
                instance = this,
                property = OpenAxisValueRestrictionSignature::startingMemberIncluded,
                condition = { VALID_STARTING_MEMBER_INCLUDED_VALUES.contains(startingMemberIncluded) },
                message = { "unsupported IsStartingMemberIncluded value '$startingMemberIncluded'" }
            )
        }
    }

    companion object {

        fun fromRow(
            row: ResultRow,
            diagnostic: Diagnostic
        ): BaselineOrdinateCategorisation {

            val databaseIdSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dimensionMemberSignatureCol,
                diagnostic
            )

            val xbrlCodeSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dpsCol,
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
            diagnostic: Diagnostic
        ): BaselineOrdinateCategorisation.Signature {
            val rawSignature = row[column] ?: diagnostic.fatal("Empty OrdinateCategorisation signature")

            val signatureMatch = SIGNATURE_PATTERN.matchEntire(rawSignature)
                ?: diagnostic.fatal("Unsupported signature in OrdinateCategorisation.${column.name}: $rawSignature")

            fun optionalSignatureComponent(partName: String) =
                (signatureMatch.groups as MatchNamedGroupCollection)[partName]?.value

            fun signaturePart(partName: String) = optionalSignatureComponent(partName)!!

            return if (optionalSignatureComponent("dim") != null) {
                BaselineOrdinateCategorisation.Signature(
                    dimensionIdentifier = signaturePart("dim"),
                    memberIdentifier = signaturePart("mem"),
                    openAxisValueRestrictionSignature = null
                )
            } else {
                BaselineOrdinateCategorisation.Signature(
                    dimensionIdentifier = signaturePart("dimOa"),
                    memberIdentifier = signaturePart("memOa"),
                    openAxisValueRestrictionSignature = OpenAxisValueRestrictionSignature(
                        hierarchyIdentifier = signaturePart("hierOa"),
                        hierarchyStartingMemberIdentifier = signaturePart("startmemOa"),
                        startingMemberIncluded = signaturePart("startMemberInclOa")
                    )
                )
            }
        }

        private val SIGNATURE_PATTERN =
            """
            \A

            (?<dim>[^\(\)]+)
                \(
                (?<mem>[^\(\)\[\]]+)
                \)

            |

            (?<dimOa>[^\(\)]+)
                \(
                (?<memOa>[^\(\)\[\]]+)
                    \[
                    (?<hierOa>[^\(\)\[\];]+)
                    ;
                    (?<startmemOa>[^\(\)\[\];]+)
                    ;
                    (?<startMemberInclOa>[^\(\)\[\];]+)
                    \]
                \)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)
    }

    override fun validate(validationResults: ValidationResults) {
        databaseIdSignature.validate(validationResults)
        xbrlCodeSignature.validate(validationResults)
    }
}
