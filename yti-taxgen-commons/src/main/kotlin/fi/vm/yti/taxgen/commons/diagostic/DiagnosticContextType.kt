package fi.vm.yti.taxgen.commons.diagostic

enum class DiagnosticContextType(val recurring: Boolean, val displayName: String) {
    CmdCompileDpmDb(false, "Compiling DPM database"),
    CmdCaptureYclSources(false, "Capturing YTI Codelist sources"),

    ActivityRecordYclSources(false, "Writing YCL sources"),
    ActivityMapYclToDpm(false, "Processing YCL sources"),
    ActivityWriteDpmDb(false, "Writing DPM database"),

    InitConfiguration(false, "Configuration file"),
    InitUriResolution(false, "URI resolution"),

    YclSource(false, "YCL Sources"),

    DpmOwner(true, "Owner"),
    DpmDictionary(true, "DPM dictionary"),

    YclCodelist(true, "Codelist"),
    YclCodesPage(true, "Codes Page"),
    YclCode(true, "Code"),
    YclCodelistExtension(true, "Codelist Extension"),
    YclCodelistExtensionMembersPage(true, "Codelist Extension Members Page"),
    YclExtensionMember(true, "Extension Member")
}
