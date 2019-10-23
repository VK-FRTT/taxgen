package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementPropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength
import fi.vm.yti.taxgen.dpmmodel.validators.validateLengths

data class ExplicitDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        validateDpmElement(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = ExplicitDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateLengths(
            validationResults = validationResults,
            instance = this,
            properties = listOf(ExplicitDomain::members, ExplicitDomain::hierarchies),
            minLength = 0,
            maxLength = 10000
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            valueProperties = listOf(Member::uri, Member::memberCode)
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "members",
            validate = { messages ->
                val count = members.count { it.defaultMember }

                if (count > 1) {
                    messages.add("has $count default members (should have at max 1)")
                }
            }
        )

        validateElementPropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            valueProperties = listOf(Hierarchy::uri, Hierarchy::hierarchyCode)
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "hierarchies",
            validate = { messages ->
                val domainMemberCodes = members.map { it.memberCode }.toSet()

                hierarchies.forEach { hierarchy ->
                    hierarchy.allNodes().forEach { node ->
                        if (!domainMemberCodes.contains(node.referencedElementCode)) {
                            messages.add(
                                "DPM HierarchyNode ${node.uri} refers to DPM Member which is not present in DPM ExplicitDomain."
                            )
                        }
                    }
                }
            }
        )
    }

    override fun code(): String = domainCode
}
