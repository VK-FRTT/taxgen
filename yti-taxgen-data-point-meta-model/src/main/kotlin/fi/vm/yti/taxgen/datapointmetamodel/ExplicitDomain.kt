package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.commons.datavalidation.validateCustom
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableKeysUnique
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

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            keyProperty = Member::memberCode
        )

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::members,
            keyProperty = Member::id
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

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            keyProperty = Hierarchy::hierarchyCode
        )

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterableProperty = ExplicitDomain::hierarchies,
            keyProperty = Hierarchy::id
        )

        validateCustom(
            validationErrors = validationErrors,
            instance = this,
            propertyName = "hierarchies",
            validate = { messages ->
                val domainMemberRefs = members.map { it.ref() }.toSet()

                hierarchies.forEach { hierarchy ->
                    val externalMemberRefs = hierarchy
                        .allNodes()
                        .map { it.memberRef }
                        .toSet()
                        .filterNot { domainMemberRefs.contains(it) }

                    if (externalMemberRefs.any()) {
                        val ids = externalMemberRefs.joinToString { it.id }
                        messages.add("Hierarchy ${hierarchy.hierarchyCode} has Members which do not belong to Domain [$ids])")
                    }
                }
            }
        )
    }
}
