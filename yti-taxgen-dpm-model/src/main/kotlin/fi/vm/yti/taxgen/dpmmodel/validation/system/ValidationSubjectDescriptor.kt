package fi.vm.yti.taxgen.dpmmodel.validation.system

data class ValidationSubjectDescriptor(
    val subjectType: String,
    val subjectIdentifiers: List<String>
)
