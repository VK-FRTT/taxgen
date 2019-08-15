package fi.vm.yti.taxgen.sqliteprovider.dictionaryreplace.ordinatecategorisationtransform

import fi.vm.yti.taxgen.commons.datavalidation.Validatable
import fi.vm.yti.taxgen.commons.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.dpmmodel.validators.validateNonNull
import org.jetbrains.exposed.dao.EntityID

data class FinalOrdinateCategorisation(
    val ordinateId: EntityID<Int>?,

    val dimensionId: EntityID<Int>?,
    val memberId: EntityID<Int>?,

    val databaseIdSignatureLiteral: String,
    val xbrlCodeSignatureLiteral: String,

    val source: String?
) : Validatable {

    companion object {

        private const val OPEN_MEMBER_MARKER = "*"

        fun fromBaseline(
            baseline: BaselineOrdinateCategorisation,
            transformationContext: TransformationContext,
            diagnostic: Diagnostic
        ): FinalOrdinateCategorisation {

            fun selectDimensionId(): EntityID<Int>? {
                return transformationContext.dimensionIdsByXbrlCodes[baseline.xbrlCodeSignature.dimensionIdentifier]
            }

            fun selectMemberId(): EntityID<Int>? {
                return if (baseline.xbrlCodeSignature.memberIdentifier == OPEN_MEMBER_MARKER) {
                    transformationContext.fixedEntitiesLookupItem.openMemberId
                } else {
                    transformationContext.memberIdsByXbrlCodes[baseline.xbrlCodeSignature.memberIdentifier]
                }
            }

            val finalCategorisation = FinalOrdinateCategorisation(
                ordinateId = baseline.ordinateId,
                dimensionId = selectDimensionId(),
                memberId = selectMemberId(),
                databaseIdSignatureLiteral = baseline.databaseIdSignature.rawSignature!!,
                xbrlCodeSignatureLiteral = baseline.xbrlCodeSignature.rawSignature!!,
                source = baseline.source
            )

            diagnostic.validate(finalCategorisation) {
                ValidatableInfo(
                    objectKind = "OrdinateCategorisation",
                    objectAddress = "OrdinateID: ${finalCategorisation.ordinateId}, DPS: ${finalCategorisation.xbrlCodeSignatureLiteral}"
                )
            }

            return finalCategorisation
        }
    }

    override fun validate(validationResults: ValidationResults) {

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = FinalOrdinateCategorisation::dimensionId
        )

        validateNonNull(
            validationResults = validationResults,
            instance = this,
            property = FinalOrdinateCategorisation::memberId
        )
    }
}
