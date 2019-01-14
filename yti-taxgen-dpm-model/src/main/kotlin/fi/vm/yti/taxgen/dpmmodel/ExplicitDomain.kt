package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateElementPropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class ExplicitDomain(
    override val uri: String,
    override val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = ExplicitDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = ExplicitDomain::members,
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
                val domainMemberUris = members.map { it.uri }.toSet()

                hierarchies.forEach { hierarchy ->
                    hierarchy.allNodes().forEach { node ->
                        if (!domainMemberUris.contains(node.referencedMemberUri)) {
                            messages.add(
                                "DPM HierarchyNode ${node.uri} refers to DPM Member ${node.referencedMemberUri} which is not part of the containing DPM ExplicitDomain."
                            )
                        }
                    }
                }
            }
        )
    }

    override fun code(): String = domainCode
}
