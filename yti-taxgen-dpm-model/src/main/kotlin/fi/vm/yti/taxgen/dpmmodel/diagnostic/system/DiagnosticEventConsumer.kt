package fi.vm.yti.taxgen.dpmmodel.diagnostic.system

import fi.vm.yti.taxgen.dpmmodel.datavalidation.ValidatableInfo
import fi.vm.yti.taxgen.dpmmodel.datavalidation.system.ValidationResultInfo

interface DiagnosticEventConsumer {

    fun contextEnter(contextStack: List<DiagnosticContextDescriptor>)
    fun contextExit(contextStack: List<DiagnosticContextDescriptor>, retiredContext: DiagnosticContextDescriptor)
    fun topContextDetailsChange(
        contextStack: List<DiagnosticContextDescriptor>,
        originalContext: DiagnosticContextDescriptor
    )

    fun message(severity: Severity, message: String)
    fun validationResults(
        validatableInfo: ValidatableInfo,
        validationResults: List<ValidationResultInfo>
    )
}
