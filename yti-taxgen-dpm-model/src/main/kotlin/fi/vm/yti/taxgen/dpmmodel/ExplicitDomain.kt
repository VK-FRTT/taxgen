package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.dpmmodel.validation.ValidationResultBuilder
import fi.vm.yti.taxgen.dpmmodel.validators.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateDpmElementCrossReferences
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableDpmElementPropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropLength
import fi.vm.yti.taxgen.dpmmodel.validators.validatePropsLengths

data class ExplicitDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationResultBuilder: ValidationResultBuilder) {

        validateDpmElement(validationResultBuilder)

        validatePropLength(
            validationResultBuilder = validationResultBuilder,
            property = this::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validatePropsLengths(
            validationResultBuilder = validationResultBuilder,
            properties = listOf(this::members, this::hierarchies),
            minLength = 0,
            maxLength = 10000
        )

        validateIterableDpmElementPropertyValuesUnique(
            validationResultBuilder = validationResultBuilder,
            property = this::members,
            elementProperties = listOf(Member::uri, Member::memberCode)
        )

        validateCustom(
            validationResultBuilder = validationResultBuilder,
            valueName = listOf(Member::class, Member::defaultMember),
            validate = { errorReporter ->
                val count = members.count { it.defaultMember }

                if (count > 1) {
                    errorReporter.error(
                        "Too many default members (maximum 1)",
                        "$count"
                    )
                }
            }
        )

        validateIterableDpmElementPropertyValuesUnique(
            validationResultBuilder = validationResultBuilder,
            property = this::hierarchies,
            elementProperties = listOf(Hierarchy::uri, Hierarchy::hierarchyCode)
        )

        hierarchies.forEach { hierarchy ->
            validationResultBuilder.withSubject(hierarchy.validationSubjectDescriptor()) {

                validateDpmElementCrossReferences(
                    validationResultBuilder = validationResultBuilder,
                    targetElements = members,
                    targetCodeProperty = Member::memberCode,
                    referringElements = hierarchy.allNodes(),
                    referringCodeProperty = HierarchyNode::referencedElementCode
                )
            }
        }
    }

    override fun code(): String = domainCode
}
