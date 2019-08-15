package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
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

    data class OpenAxisValueRestrictionSignature(
        val rawSignature: String?,
        val hierarchyIdentifier: String,
        val hierarchyStartingMemberIdentifier: String,
        val startingMemberIncluded: Boolean
    )

    data class Signature(
        val rawSignature: String?,
        val dimensionIdentifier: String,
        val memberIdentifier: String,
        val openAxisValueRestrictionSignature: OpenAxisValueRestrictionSignature?
    )

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

            // TODO - derive + validate equality with original xbrlCodeSignature
            // val derivedXbrlCodeSignature = deriveXbrlCodeSignatureFromDatabaseIdSignature(databaseIdSignature)

            val baselineCategorisation = BaselineOrdinateCategorisation(
                ordinateId = row[OrdinateCategorisationTable.ordinateIdCol],
                databaseIdSignature = databaseIdSignature,
                xbrlCodeSignature = xbrlCodeSignature,
                source = row[OrdinateCategorisationTable.sourceCol]
            )

            diagnostic.validate(baselineCategorisation) {
                ValidatableInfo(
                    objectKind = "BaselineOrdinateCategorisation",
                    objectAddress = "OrdinateID: ${baselineCategorisation.ordinateId}, DPS: ${baselineCategorisation.xbrlCodeSignature.rawSignature}"
                )
            }

            return baselineCategorisation
        }

        private fun tokenizeSignature(
            row: ResultRow,
            column: Column<String?>,
            diagnostic: Diagnostic
        ): BaselineOrdinateCategorisation.Signature {
            val rawSignature = row[column] ?: diagnostic.fatal("Empty OrdinateCategorisation signature")

            val signatureMatch = SIGNATURE_PATTERN.matchEntire(rawSignature)
                ?: diagnostic.fatal("Unsupported signature in OrdinateCategorisation.${column.name}: $rawSignature")

            val signatureMatchGroups = signatureMatch.groups as MatchNamedGroupCollection

            return if (signatureMatchGroups["dim"] != null) {
                BaselineOrdinateCategorisation.Signature(
                    rawSignature = rawSignature,
                    dimensionIdentifier = signatureMatchGroups["dim"]!!.value,
                    memberIdentifier = signatureMatchGroups["mem"]!!.value,
                    openAxisValueRestrictionSignature = null
                )
            } else {
                BaselineOrdinateCategorisation.Signature(
                    rawSignature = rawSignature,
                    dimensionIdentifier = signatureMatchGroups["dim2"]!!.value,
                    memberIdentifier = signatureMatchGroups["mem2"]!!.value,
                    openAxisValueRestrictionSignature = null
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

            (?<dim2>[^\(\)]+)
                \(
                (?<mem2>[^\(\)\[\]]+)
                    \[
                    (?<hier2>[^\(\)\[\];]+)
                    ;
                    (?<startmem2>[^\(\)\[\];]+)
                    ;
                    (?<isincl2>[^\(\)\[\];]+)
                    \]
                \)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)
    }

    override fun validate(validationResults: ValidationResults) {
        validateNonBlank(
            validationResults = validationResults,
            instance = databaseIdSignature,
            property = Signature::dimensionIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = databaseIdSignature,
            property = Signature::memberIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = xbrlCodeSignature,
            property = Signature::dimensionIdentifier
        )

        validateNonBlank(
            validationResults = validationResults,
            instance = xbrlCodeSignature,
            property = Signature::memberIdentifier
        )
    }
}
