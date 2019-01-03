package fi.vm.yti.taxgen.rdsprovider.contextdiagnostic

import fi.vm.yti.taxgen.commons.diagostic.DiagnosticContext
import fi.vm.yti.taxgen.rdsprovider.CodeListBlueprint
import fi.vm.yti.taxgen.rdsprovider.CodeListSource
import fi.vm.yti.taxgen.rdsprovider.ExtensionSource

internal class CodeListSourceContextDecorator(
    private val codeListSource: CodeListSource,
    private val diagnosticContext: DiagnosticContext
) : CodeListSource {

    override fun blueprint(): CodeListBlueprint = codeListSource.blueprint()

    override fun contextLabel(): String = codeListSource.contextLabel()
    override fun contextIdentifier(): String = codeListSource.contextIdentifier()

    override fun codeListMetaData(): String = codeListSource.codeListMetaData()

    override fun codePagesData(): Sequence<String> = codeListSource.codePagesData()

    override fun extensionSources(): Sequence<ExtensionSource> = codeListSource.extensionSources()

    override fun subCodeListSources(): Sequence<CodeListSource> = codeListSource.subCodeListSources()
}
