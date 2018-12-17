package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterablePropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class ExplicitDomain(
    override val id: String,
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
            minLength = 1,
            maxLength = 10000
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            valueProperty = Member::id
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            valueProperty = Member::memberCode
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "members",
            validate = { messages ->
                val count = members.count { it.defaultMember }

                if (count != 1) {
                    messages.add("has $count default members (should have 1)")
                }
            }
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            valueProperty = Hierarchy::id
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            valueProperty = Hierarchy::hierarchyCode
        )

        validateCustom(
            validationResults = validationResults,
            instance = this,
            propertyName = "hierarchies",
            validate = { messages ->
                val domainMemberRefs = members.map { it.ref() }.toSet()

                hierarchies.forEach { hierarchy ->
                    hierarchy
                        .allNodes()
                        .toSet()
                        .filterNot { domainMemberRefs.contains(it.memberRef) }
                        .forEach { node ->
                            messages.add(
                                "DPM Hierarchy contains to DPM Member, which is not part of the DPM Domain. " +
                                    "${node.memberRef.diagnosticTag()} in ${node.ref().diagnosticTag()} at ${hierarchy.ref().diagnosticTag()}"
                            )
                        }
                }
            }
        )
    }
}
