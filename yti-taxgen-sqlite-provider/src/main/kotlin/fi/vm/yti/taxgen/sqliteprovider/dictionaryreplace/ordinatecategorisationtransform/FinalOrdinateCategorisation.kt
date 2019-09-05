package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import org.jetbrains.exposed.dao.EntityID

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
            require(signature.type == OrdinateCategorisationSignature.Type.XBRL_CODE_SIGNATURE)

            return doComposeSignatureLiteral(
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                hasOpenAxisValueRestriction = (dbReferences.openAxisValueRestrictionDbReferences != null),
                hierarchy = dbReferences.openAxisValueRestrictionDbReferences?.hierarchyId,
                startingMember = dbReferences.openAxisValueRestrictionDbReferences?.hierarchyStartingMemberId,
                startingMemberIncluded = signature.openAxisValueRestrictionSignature?.startingMemberIncluded
            )
        }

        private fun composeXbrlCodeSignatureLiteral(
            signature: OrdinateCategorisationSignature
        ): String {
            require(signature.type == OrdinateCategorisationSignature.Type.XBRL_CODE_SIGNATURE)

            return doComposeSignatureLiteral(
                dimension = signature.dimensionIdentifier,
                member = signature.memberIdentifier,
                hasOpenAxisValueRestriction = (signature.openAxisValueRestrictionSignature != null),
                hierarchy = signature.openAxisValueRestrictionSignature?.hierarchyIdentifier,
                startingMember = signature.openAxisValueRestrictionSignature?.hierarchyStartingMemberIdentifier,
                startingMemberIncluded = signature.openAxisValueRestrictionSignature?.startingMemberIncluded
            )
        }

        private fun doComposeSignatureLiteral(
            dimension: String,
            member: String,
            hasOpenAxisValueRestriction: Boolean,
            hierarchy: Any?,
            startingMember: Any?,
            startingMemberIncluded: Any?
        ): String {
            return if (hasOpenAxisValueRestriction) {
                "$dimension($member[$hierarchy;$startingMember;$startingMemberIncluded])"
            } else {
                "$dimension($member)"
            }
        }
    }

    override fun validate(validationResults: ValidationResults) {

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = FinalOrdinateCategorisation::ordinateId
        )

        dbReferences.validate(validationResults)
    }
}
