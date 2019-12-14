package fi.vm.yti.taxgen.dpmmodel.validation

interface ValidationErrorReporter {

    fun error(
        reason: String,
        value: String
    )
}
