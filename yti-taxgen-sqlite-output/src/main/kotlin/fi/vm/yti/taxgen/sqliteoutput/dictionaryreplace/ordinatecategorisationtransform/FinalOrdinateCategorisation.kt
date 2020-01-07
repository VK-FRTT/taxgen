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
                signatureStructure = signature.signatureStructure,
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                hierarchy = dbReferences.hierarchyId,
                startingMember = dbReferences.hierarchyStartingMemberId,
                startingMemberIncluded = signature.startingMemberIncluded
            )
        }

        private fun composeXbrlCodeSignatureLiteral(
            signature: OrdinateCategorisationSignature
        ): String {
            require(signature.identifierKind == OrdinateCategorisationSignature.IdentifierKind.XBRL_CODE)

            return doComposeSignatureLiteral(
                signatureStructure = signature.signatureStructure,
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                hierarchy = signature.hierarchyIdentifier,
                startingMember = signature.hierarchyStartingMemberIdentifier,
                startingMemberIncluded = signature.startingMemberIncluded
            )
        }

        private fun doComposeSignatureLiteral(
            signatureStructure: OrdinateCategorisationSignatureStructure,
            dimension: String,
            member: String,
            hierarchy: Any?,
            startingMember: Any?,
            startingMemberIncluded: Any?
        ): String {
            return when (signatureStructure) {
                OrdinateCategorisationSignatureStructure.NO_OPEN_AXIS_VALUE_RESTRICTION -> {
                    "$dimension($member)"
                }
                OrdinateCategorisationSignatureStructure.PARTIAL_OPEN_AXIS_VALUE_RESTRICTION -> {
                    "$dimension($member?[$hierarchy])"
                }
                OrdinateCategorisationSignatureStructure.FULL_OPEN_AXIS_VALUE_RESTRICTION -> {
                    "$dimension($member[$hierarchy;$startingMember;$startingMemberIncluded])"
                }
            }
        }
    }

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateNonNull(
            validationResultBuilder = validationResultBuilder,
            property = this::ordinateId
        )

        dbReferences.validate(validationResultBuilder)
    }

    override fun validationSubjectDescriptor(): ValidationSubjectDescriptor {
        return ValidationSubjectDescriptor(
            subjectType = "OrdinateCategorisation (transformed)",
            subjectIdentifier = "OrdinateID: $ordinateId"
        )
    }
}
