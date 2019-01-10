package fi.vm.yti.taxgen.rdsdpmmapper.sourcereader

import fi.vm.yti.taxgen.commons.JsonOps
import fi.vm.yti.taxgen.commons.diagostic.Diagnostic
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCode
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCodeListMeta
import fi.vm.yti.taxgen.rdsdpmmapper.rdsmodel.RdsCodePage
import fi.vm.yti.taxgen.rdsprovider.CodeListSource

internal class CodeListSourceReader(
    private val codeListSource: CodeListSource,
    private val diagnostic: Diagnostic

) {
    private val codeListMeta: RdsCodeListMeta by lazy {
        JsonOps.readValue<RdsCodeListMeta>(codeListSource.codeListMetaData(), diagnostic)
    }

    fun codeListMeta(): RdsCodeListMeta {
        return codeListMeta
    }

    fun eachCode(action: (RdsCode) -> Unit) {
        codeListSource.eachCodePageData { pageData ->
            val codePage = JsonOps.readValue<RdsCodePage>(pageData, diagnostic)
            codePage.results?.forEach(action)
        }
    }

    fun eachExtensionSource(action: (ExtensionSourceReader) -> Unit) {
        codeListSource.eachExtensionSource { extensionSource ->
            val reader = ExtensionSourceReader(extensionSource, diagnostic)

            diagnostic.updateCurrentContextDetails(
                label = reader.extensionMetaData().diagnosticLabel()
            )

            action(reader)
        }
    }

    fun eachSubCodeListSource(action: (CodeListSourceReader) -> Unit) {
        codeListSource.eachSubCodeListSource { subCodeListSource ->
            val reader = CodeListSourceReader(subCodeListSource, diagnostic)

            diagnostic.updateCurrentContextDetails(
                label = reader.codeListMeta().diagnosticLabel()
            )

            action(reader)
        }
    }
}
