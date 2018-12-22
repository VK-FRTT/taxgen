package fi.vm.yti.taxgen.commons.diagostic

enum class DiagnosticContextType(val recurring: Boolean, val displayName: String) {
    CmdCompileDpmDb(false, "Compiling DPM database"),
    CmdCaptureDpmSources(false, "Capturing DPM sources"),

    RecordingDpmSource(false, "Writing DPM sources"),
    MappingRdsToDpm(false, "Processing RDS sources"),
    WritingSQLiteDpmDb(false, "Writing DPM database"),

    InitConfiguration(false, "Configuration file"),
    InitContentUrls(false, "Content URLs"),

    DpmSource(false, "DPM Sources"),
    DpmOwner(true, "Owner"),
    DpmDictionary(true, "DPM dictionary"),

    RdsCodelist(true, "Codelist"),
    RdsCodesPage(true, "Codes Page"),
    RdsCode(true, "Code"),
    RdsCodelistExtension(true, "Codelist Extension"),
    RdsCodelistExtensionMembersPage(true, "Codelist Extension Members Page"),
    RdsExtensionMember(true, "Extension Member")
}
