package fi.vm.yti.taxgen.commons.diagnostic

import fi.vm.yti.taxgen.dpmmodel.diagnostic.DiagnosticContextType

enum class DiagnosticContexts(val recurringContext: Boolean, val displayName: String) {
    CmdWriteDictionariesToDpmDb(false, "Writing dictionaries to DPM database"),
    CmdCaptureDpmSources(false, "Capturing DPM sources"),

    DpmSourceRecorder(false, "DPM source recorder"),
    RdsToDpmMapper(false, "RDS to DPM mapper"),
    SQLiteDbWriter(false, "SQLite database writer"),

    InitConfiguration(false, "Configuration file"),
    InitContentAddress(false, "Content URLs"),

    DpmSource(false, "DPM source"),
    DpmDictionary(true, "DPM dictionary"),

    RdsCodeList(true, "Codelist"),
    RdsCodesPage(true, "Codes Page"),
    RdsExtension(true, "Extension"),
    RdsExtensionMembersPage(true, "Extension members page");

    fun toType() = DiagnosticContextType(
        typeName = this.name,
        recurringContext = this.recurringContext
    )
}
