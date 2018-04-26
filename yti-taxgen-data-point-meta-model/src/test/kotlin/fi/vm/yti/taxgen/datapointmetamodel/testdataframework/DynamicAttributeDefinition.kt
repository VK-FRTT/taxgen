package fi.vm.yti.taxgen.datapointmetamodel.testdataframework

fun dynamicAttribute(valueMaker: (DynamicAttributeContext) -> Any?): DynamicAttributeDefinition =
    DynamicAttributeDefinition(valueMaker)

data class DynamicAttributeDefinition(val valueMaker: (DynamicAttributeContext) -> Any?)
