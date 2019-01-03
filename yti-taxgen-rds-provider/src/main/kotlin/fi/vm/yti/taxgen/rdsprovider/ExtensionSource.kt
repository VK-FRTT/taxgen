package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails

interface ExtensionSource : DiagnosticContextDetails {
    fun extensionMetaData(): String
    fun extensionMemberPagesData(): Sequence<String>
}
