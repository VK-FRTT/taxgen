package fi.vm.yti.taxgen.commons.diagostic

enum class DiagnosticContextType(val recurring: Boolean, val displayName: String) {
    CmdCompileDpmDb(false, "Compiling DPM database"),
    CmdCaptureDpmSources(false, "Capturing DPM sources"),

    CaptureDpmSource(false, "Writing DPM sources"),
    MappingRdsToDpm(false, "Processing RDS sources"),
    WritingSQLiteDpmDb(false, "Writing DPM database"),

    InitConfiguration(false, "Configuration file"),
    InitContentAddress(false, "Content URLs"),

    DpmSource(false, "DPM Sources"),
    DpmOwner(true, "Owner"), //TODO - usage
    DpmDictionary(true, "DPM dictionary"),

    RdsCodeList(true, "Codelist"),
    RdsCodesPage(true, "Codes Page"),
    RdsExtension(true, "Extension"),
    RdsExtensionMembersPage(true, "Extension Members Page"),
}
