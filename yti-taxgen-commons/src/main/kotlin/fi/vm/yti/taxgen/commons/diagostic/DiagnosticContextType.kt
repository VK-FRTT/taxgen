package fi.vm.yti.taxgen.commons.diagostic

enum class DiagnosticContextType(val displayName: String) {
    CmdCompileDpmDb("Compiling DPM database"),
    CmdCaptureYclSources("Capturing YTI Codelist sources"),

    ActivityRecordYclSources("Writing YCL sources"),
    ActivityMapYclToDpm("Processing YCL sources"),
    ActivityWriteDpmDb("Writing DPM database"),

    InitConfiguration("Configuration file"),
    InitUriResolution("URI resolution"),

    DpmOwner("Owner"),
    DpmDictionary("DPM dictionary"),

    YclSource("YCL Sources"),
    YclCodelist("Codelist"),
    YclCodesPage("Codes Page"),
    YclCode("YCL Code"),
    YclCodelistExtension("Codelist Extension"),
    YclCodelistExtensionMembersPage("Codelist Extension Members Page")
}
