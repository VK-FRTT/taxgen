package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableKeysUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateLength

data class Hierarchy(
    override val id: String,
    override val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : DpmElement {

    override fun validate(validationErrors: ValidationErrors) {

        super.validate(validationErrors)

        validateLength(
            validationErrors = validationErrors,
            instance = this,
            property = Hierarchy::hierarchyCode,
            minLength = 2,
            maxLength = 50
        )

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterable = allNodes(),
            iterablePropertyName = "rootNodes",
            keySelector = { it.id },
            errorMessageBuilder = { duplicates -> "id has duplicate values [${duplicates.joinToString { it }}]" }
        )

        validateIterableKeysUnique(
            validationErrors = validationErrors,
            instance = this,
            iterable = allNodes(),
            iterablePropertyName = "rootNodes",
            keySelector = { it.memberRef.id },
            errorMessageBuilder = { duplicates -> "multiple HierarchyNodes referring same Members [${duplicates.joinToString { it }}]" }
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.allNodes() }
            .flatten()
    }
}
