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

    RdsCodeList(true, "Codelist"),
    RdsCodesPage(true, "Codes Page"),       //TODO - Filter out?
    RdsCode(true, "Code"),
    RdsExtension(true, "Extension"),
    RdsExtensionMembersPage(true, "Extension Members Page"), //TODO - Filter out?
    RdsExtensionMember(true, "Extension Member")
}
