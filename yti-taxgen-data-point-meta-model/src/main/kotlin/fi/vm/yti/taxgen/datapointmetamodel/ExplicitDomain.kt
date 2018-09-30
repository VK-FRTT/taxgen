package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterablePropertyValuesUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class ExplicitDomain(
    override val id: String,
    override val concept: Concept,
    val domainCode: String,
    val members: List<Member>,
    val hierarchies: List<Hierarchy>
) : DpmElement {

    override fun validate(validationErrors: ValidationErrors) {

        super.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::domainCode,
            minLength = 2,
            maxLength = 50
        )

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = ExplicitDomain::members,
            minLength = 1,
            maxLength = 10000
        )

        validateIterablePropertyValuesUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            valueProperty = Member::memberCode
        )

        validateIterablePropertyValuesUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            valueProperty = Member::id
        )

        validateCustom(
            validationErrors = validationErrors,
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
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            valueProperty = Hierarchy::hierarchyCode
        )

        validateIterablePropertyValuesUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            valueProperty = Hierarchy::id
        )

        validateCustom(
            validationErrors = validationErrors,
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
                            messages.add("member not part of domain '${node.memberRef.diagnosticHandle()}' (hierarchy '${hierarchy.diagnosticHandle()}' / hierachy node '${node.diagnosticHandle()}')")
                        }
                }
            }
        )
    }
}
