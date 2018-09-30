package fi.vm.yti.taxgen.datapointmetamodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationErrors
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterableElementsUnique
import fi.vm.yti.taxgen.datapointmetamodel.validators.validateIterablePropertyValuesUnique
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

        validateIterablePropertyValuesUnique(
            validationErrors = validationErrors,
            instance = this,
            instancePropertyName = "rootNodes",
            iterable = allNodes(),
            valueProperty = HierarchyNode::id
        )

        validateIterableElementsUnique(
            validationErrors = validationErrors,
            instance = this,
            propertyName = "rootNodes",
            iterable = allNodes(),
            keySelector = { it.memberRef.id },
            message = { duplicateNode -> "duplicate member reference '${duplicateNode.memberRef.diagnosticHandle()}' (from '${duplicateNode.diagnosticHandle()}')" }
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.allNodes() }
            .flatten()
    }
}
