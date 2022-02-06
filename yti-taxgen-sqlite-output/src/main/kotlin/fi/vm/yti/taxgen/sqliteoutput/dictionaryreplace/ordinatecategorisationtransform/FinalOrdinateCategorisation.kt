package fi.vm.yti.taxgen.sqliteoutput.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.dpmmodel.validation.Validatable
import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validation.system.ValidationSubjectDescriptor
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import org.jetbrains.exposed.dao.id.EntityID

data class FinalOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    val dbReferences: OrdinateCategorisationDbReferences,

    val databaseIdSignature: String,
    val xbrlCodeSignature: String,

    val source: String?
) : Validatable {

    companion object {

        fun fromBaseline(
            baseline: BaselineOrdinateCategorisation
        ): FinalOrdinateCategorisation {
            val dbReferences =
                OrdinateCategorisationDbReferences.fromOrdinateCategorisationXbrlCodeSignature(
                    baseline.xbrlCodeSignature
                )

            return FinalOrdinateCategorisation(
                ordinateId = baseline.ordinateId,
                source = baseline.source,
                dbReferences = dbReferences,
                databaseIdSignature = composeDbIdSignatureLiteral(
                    baseline.xbrlCodeSignature,
                    dbReferences
                ),
                xbrlCodeSignature = composeXbrlCodeSignatureLiteral(
                    baseline.xbrlCodeSignature
                )
            )
        }

        private fun composeDbIdSignatureLiteral(
            signature: OrdinateCategorisationSignature,
            dbReferences: OrdinateCategorisationDbReferences
        ): String {
            require(signature.identifierKind == OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE)

            return doComposeSignatureLiteral(
                signaturePrecision = signature.signaturePrecision,
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                defaultMemberIncluded = signature.isDefaultMemberIncluded,
                hierarchy = dbReferences.hierarchyId,
                startingMember = dbReferences.hierarchyStartingMemberId,
                startingMemberIncluded = signature.isStartingMemberIncluded
            )
        }

        private fun composeXbrlCodeSignatureLiteral(
            signature: OrdinateCategorisationSignature
        ): String {
            require(signature.identifierKind == OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE)

            return doComposeSignatureLiteral(
                signaturePrecision = signature.signaturePrecision,
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                defaultMemberIncluded = signature.isDefaultMemberIncluded,
                hierarchy = signature.hierarchyIdentifier,
                startingMember = signature.hierarchyStartingMemberIdentifier,
                startingMemberIncluded = signature.isStartingMemberIncluded
            )
        }

        private fun doComposeSignatureLiteral(
            signaturePrecision: OrdinateCategorisationSignature.SignaturePrecision,
            dimension: String,
            member: String,
            defaultMemberIncluded: String?,
            hierarchy: Any?,
            startingMember: Any?,
            startingMemberIncluded: Any?
        ): String {
            return when (signaturePrecision) {
                OrdinateCategorisationSignature.SignaturePrecision.CLOSED_AXIS -> {
                    "$dimension($member)"
                }
                OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_PARTIAL_RESTRICTION -> {
                    "$dimension($member$defaultMemberIncluded[$hierarchy])"
                }
                OrdinateCategorisationSignature.SignaturePrecision.SEMI_OPEN_AXIS_FULL_RESTRICTION -> {
                    "$dimension($member$defaultMemberIncluded[$hierarchy;$startingMember;$startingMemberIncluded])"
                }
            }
        }
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateNonNull(
            validationResultBuilder = validationResultBuilder,
            property = this::ordinateId
        )

        validationResultBuilder.validateNestedProperty(this::dbReferences)
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OrdinateCategorisation (transformed)",
            subjectIdentifiers = listOf(
                "OrdinateID: $ordinateId",
                "BaselineDPS: ${dbReferences.signature.originSignatureLiteral}"
            )
        )
    }
}
