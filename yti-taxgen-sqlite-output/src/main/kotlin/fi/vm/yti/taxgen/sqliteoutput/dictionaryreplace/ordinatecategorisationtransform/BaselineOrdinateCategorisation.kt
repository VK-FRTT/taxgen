package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.thisShouldNeverHappen
import fi.vm.yti.taxgen.dpmmodel.diagnostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateCustom
import fi.vm.yti.taxgen.sqliteoutput.tables.OrdinateCategorisationTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow

data class BaselineOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    // Tokenized from mOrdinateCategorisation.DimensionMemberSignature
    val databaseIdSignature: OrdinateCategorisationSignature,

    // Tokenized from mOrdinateCategorisation.DPS
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
                OrdinateCategorisationSignature.IdentifierKind.DATABASE_ID,
                diagnostic
            )

            val xbrlCodeSignature = tokenizeSignature(
                row,
                OrdinateCategorisationTable.dpsCol,
                OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE,
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
            identifierKind: OrdinateCategorisationSignature.IdentifierKind,
            diagnostic: Diagnostic
        ): OrdinateCategorisationSignature {
            val signatureLiteral = row[column] ?: diagnostic.fatal("Empty OrdinateCategorisation signature")

            val signatureMatch = SIGNATURE_PATTERN.matchEntire(signatureLiteral)
                ?: diagnostic.fatal("Unsupported signature in OrdinateCategorisation.${column.name}: $signatureLiteral")

            fun signatureElementValueOrNull(partName: String) =
                (signatureMatch.groups as MatchNamedGroupCollection)[partName]?.value

            fun signatureElementValue(partName: String) = signatureElementValueOrNull(partName)
                ?: thisShouldNeverHappen("SignaturePattern configuration mismatch")

            return when {
                signatureElementValueOrNull("ClosedAxisDimension") != null -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signaturePrecision = OrdinateCategorisationSignature.SignaturePrecision.CLOSED_AXIS,
                        dimensionIdentifier = signatureElementValue("ClosedAxisDimension"),
                        memberIdentifier = signatureElementValue("ClosedAxisMember"),
                        isDefaultMemberIncluded = null,
                        hierarchyIdentifier = null,
                        hierarchyStartingMemberIdentifier = null,
                        isStartingMemberIncluded = null,
                        originSignatureLiteral = signatureLiteral
                    )
                }

                (signatureElementValueOrNull("SoAxisPartialDimension") != null) -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signaturePrecision = OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION,
                        dimensionIdentifier = signatureElementValue("SoAxisPartialDimension"),
                        memberIdentifier = signatureElementValue("SoAxisPartialMember"),
                        isDefaultMemberIncluded = signatureElementValue("SoAxisPartialDefMemberIncluded"),
                        hierarchyIdentifier = signatureElementValue("SoAxisPartialHierarchy"),
                        hierarchyStartingMemberIdentifier = null,
                        isStartingMemberIncluded = null,
                        originSignatureLiteral = signatureLiteral
                    )
                }

                (signatureElementValueOrNull("SoAxisFullDimension") != null) -> {
                    OrdinateCategorisationSignature(
                        identifierKind = identifierKind,
                        signaturePrecision = OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION,
                        dimensionIdentifier = signatureElementValue("SoAxisFullDimension"),
                        memberIdentifier = signatureElementValue("SoAxisFullMember"),
                        isDefaultMemberIncluded = signatureElementValue("SoAxisFullDefMemberIncluded"),
                        hierarchyIdentifier = signatureElementValue("SoAxisFullHierarchy"),
                        hierarchyStartingMemberIdentifier = signatureElementValue("SoAxisFullStartMember"),
                        isStartingMemberIncluded = signatureElementValue("SoAxisFullStartMemberIncluded"),
                        originSignatureLiteral = signatureLiteral
                    )
                }

                else -> {
                    thisShouldNeverHappen("Signature tokenizer mismatch.")
                }
            }
        }

        private val SIGNATURE_PATTERN =
            """
            \A

            (?<ClosedAxisDimension>[^\(\)]+)
                \(
                (?<ClosedAxisMember>[^\(\)\[\]]+)
                \)

            |

            (?<SoAxisPartialDimension>[^\(\)]+)
                \(
                (?<SoAxisPartialMember>[^\(\)\[\]\?]+)
                (?<SoAxisPartialDefMemberIncluded>[?]?)
                    \[
                    (?<SoAxisPartialHierarchy>[^\(\)\[\];]+)
                    \]
                \)

            |

            (?<SoAxisFullDimension>[^\(\)]+)
                \(
                (?<SoAxisFullMember>[^\(\)\[\]\?]+)
                (?<SoAxisFullDefMemberIncluded>[?]?)
                    \[
                    (?<SoAxisFullHierarchy>[^\(\)\[\];]+)
                    ;
                    (?<SoAxisFullStartMember>[^\(\)\[\];]+)
                    ;
                    (?<SoAxisFullStartMemberIncluded>[^\(\)\[\];]+)
                    \]
                \)
            \z
            """.trimIndent().toRegex(RegexOption.COMMENTS)
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {
        validationResultBuilder.validateNestedProperty(this::databaseIdSignature)
        validationResultBuilder.validateNestedProperty(this::xbrlCodeSignature)

        validateCustom(
            validationResultBuilder = validationResultBuilder,
            valueName = "DimensionMemberSignature, DPS",
            validate = { errorReporter ->
                val mismatchDescriptions = checkSignaturesMatching()

                if (mismatchDescriptions.any()) {
                    errorReporter.error(
                        reason = "Signatures do not match",
                        value = mismatchDescriptions.joinToString()
                    )
                }
            }
        )
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OrdinateCategorisation (baseline)",
            subjectIdentifiers = listOf("OrdinateID: $ordinateId")
        )
    }

    private fun checkSignaturesMatching(): List<String> {
        val descriptions = mutableListOf<String>()

        fun checkSignatureElementsMatching(valueA: String?, valueB: String?, elementDescription: String) {
            if (valueA != valueB) {
                descriptions.add(elementDescription)
            }
        }

        checkSignatureElementsMatching(
            databaseIdSignature.dimensionIdentifier,
            xbrlCodeSignature.dimensionIdentifier,
            "Dimensions not same, DB Dimension identifier `${databaseIdSignature.dimensionIdentifier}´ XBRL Dimension identifier `${xbrlCodeSignature.dimensionIdentifier}´"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.memberIdentifier,
            xbrlCodeSignature.memberIdentifier,
            "Members not same, DB Member identifier `${databaseIdSignature.memberIdentifier}´ XBRL Member identifier `${xbrlCodeSignature.memberIdentifier}´"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.isDefaultMemberIncluded,
            xbrlCodeSignature.isDefaultMemberIncluded,
            "Default Member inclusion not same, DB inclusion `${databaseIdSignature.isDefaultMemberIncluded}´ XBRL inclusion `${xbrlCodeSignature.isDefaultMemberIncluded}´"
        )

        val dbHierarchyCode = databaseIdSignature.lookupHierarchyCodeForHierarchyIdentifier()
        checkSignatureElementsMatching(
            dbHierarchyCode,
            xbrlCodeSignature.hierarchyIdentifier,
            "Hierarchies not same, DB Hierarchy identifier `$dbHierarchyCode´ XBRL Hierarchy identifier `${xbrlCodeSignature.hierarchyIdentifier}´"
        )

        val dbMemberCode = databaseIdSignature.lookupMemberCodeForHierarchyStartingMemberIdentifier()
        checkSignatureElementsMatching(
            dbMemberCode,
            xbrlCodeSignature.hierarchyStartingMemberIdentifier,
            "Hierarchy starting Members not same, DB Member identifier `$dbMemberCode´ XBRL Member identifier `${xbrlCodeSignature.hierarchyStartingMemberIdentifier}´"
        )

        checkSignatureElementsMatching(
            databaseIdSignature.isStartingMemberIncluded,
            xbrlCodeSignature.isStartingMemberIncluded,
            "Starting member inclusion not same, DB inclusion `${databaseIdSignature.isStartingMemberIncluded}´ XBRL inclusion `${xbrlCodeSignature.isStartingMemberIncluded}´"
        )

        return descriptions
    }
}
