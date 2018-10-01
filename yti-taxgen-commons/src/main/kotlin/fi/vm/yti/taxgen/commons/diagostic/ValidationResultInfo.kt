package fi.vm.yti.taxgen.commons.diagostic

data class ValidationResultInfo(
    val className: String,
    val propertyName: String,
    val message: String
)
