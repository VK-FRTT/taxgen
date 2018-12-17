package fi.vm.yti.taxgen.dpmmodel

import fi.vm.yti.taxgen.commons.datavalidation.ValidationResults
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterableElementsUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateIterablePropertyValuesUnique
import fi.vm.yti.taxgen.dpmmodel.validators.validateLength

data class Hierarchy(
    override val id: String,
    override val uri: String,
    override val concept: Concept,
    val hierarchyCode: String,
    val rootNodes: List<HierarchyNode>
) : DpmElement {

    override fun validate(validationResults: ValidationResults) {

        super.validate(validationResults)

        validateLength(
            validationResults = validationResults,
            instance = this,
            property = Hierarchy::hierarchyCode,
            minLength = 2,
            maxLength = 50
        )

        validateIterablePropertyValuesUnique(
            validationResults = validationResults,
            instance = this,
            instancePropertyName = "rootNodes",
            iterable = allNodes(),
            valueProperty = HierarchyNode::id
        )

        validateIterableElementsUnique(
            validationResults = validationResults,
            instance = this,
            propertyName = "rootNodes",
            iterable = allNodes(),
            keySelector = { it.memberRef.id },
            message = { duplicateNode ->
                "DPM Hierarchy contains multiple times same DPM Member. " +
                    "${duplicateNode.memberRef.diagnosticTag()} in ${duplicateNode.ref().diagnosticTag()}"
            }
        )
    }

    fun allNodes(): List<HierarchyNode> {
        return rootNodes
            .map { it.allNodes() }
            .flatten()
    }
}
