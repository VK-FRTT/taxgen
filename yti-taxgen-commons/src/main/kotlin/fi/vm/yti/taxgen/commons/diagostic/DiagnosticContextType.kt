package fi.vm.yti.taxgen.commons.diagostic

enum class DiagnosticContextType(val recurring: Boolean, val displayName: String) {
    CmdCompileDpmDb(false, "Compiling DPM database"),
    CmdCaptureDpmSources(false, "Capturing DPM sources"),

    CaptureDpmSource(false, "Writing DPM sources"),
    MapRdsToDpm(false, "Processing RDS sources"),
    WriteSQLiteDb(false, "Writing DPM database"),

    InitConfiguration(false, "Configuration file"),
    InitContentAddress(false, "Content URLs"),

    DpmSource(false, "DPM Sources"),
    DpmOwner(true, "Owner"), //TODO - usage
    DpmDictionary(true, "DPM dictionary"),

    RdsCodeList(true, "Codelist"),
    RdsCodesPage(true, "Codes Page"), //TODO - usage
    RdsExtension(true, "Extension"), //TODO - usage
    RdsExtensionMembersPage(true, "Extension Members Page"),
}
