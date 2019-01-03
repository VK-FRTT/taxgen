package fi.vm.yti.taxgen.rdsprovider

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContextDetails

interface CodeListSource : DiagnosticContextDetails {
    fun blueprint(): CodeListBlueprint
    fun codeListMetaData(): String
    fun codePagesData(): Sequence<String>
    fun extensionSources(): Sequence<ExtensionSource>
    fun subCodeListSources(): Sequence<CodeListSource>
}
